package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Fire;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class FireManager {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Fire");

	private final Plugin plugin;
	private final FireStorage storage;
	private final AshManager ash;
	private final CollapseManager collapse;
	private final HeatAura heatAura;
	private final WeakBlockBurster burster;
	private final Backdraft backdraft;
	private final Map<BlockKey, BurningBlock> burning = new HashMap<>();
	private final Map<ChunkRef, Set<BlockKey>> byChunk = new HashMap<>();
	private final ArrayDeque<BlockKey> pending = new ArrayDeque<>();
	private final Map<BlockKey, Long> flashovers = new HashMap<>();
	private final List<Runnable> shutdownHooks = new ArrayList<>();
	private final Map<BlockKey, Long> ephemeralFlames = new HashMap<>();
	private final Map<ChunkRef, Map<BlockKey, HotSpot>> hotSpots = new HashMap<>();

	private FireSettings settings;
	private long tick;
	private BukkitTask task;
	private int particleBudget;

	public FireManager(Plugin plugin) {
		this.plugin = plugin;
		this.storage = new FireStorage(plugin);
		this.ash = new AshManager(plugin, this);
		this.collapse = new CollapseManager(plugin, this);
		this.heatAura = new HeatAura(this);
		this.burster = new WeakBlockBurster(this);
		this.backdraft = new Backdraft(plugin, this);
		this.settings = FireSettings.load(plugin.getConfig());
	}

	public void start() {
		final String logStep = "start";

		task = Bukkit.getScheduler().runTaskTimer(plugin, this::cycle, settings.updateInterval(), settings.updateInterval());

		for(World world : Bukkit.getWorlds()) {
			if(isWorldEnabled(world)) {
				for(Chunk chunk : world.getLoadedChunks()) {
					loadChunk(chunk);
				}
			}
		}

		_log.info("[{}] Realistic fire {} in worlds {}", logStep, settings.enabled() ? "enabled" : "disabled", settings.worlds());
	}

	public void addShutdownHook(Runnable hook) {
		shutdownHooks.add(hook);
	}

	public void shutdown() {
		shutdownHooks.forEach(Runnable::run);
		stop();
	}

	public void stop() {
		if(task != null) {
			task.cancel();
			task = null;
		}

		for(ChunkRef ref : new ArrayList<>(byChunk.keySet())) {
			final World world = Bukkit.getWorld(ref.world());
			if(world != null && world.isChunkLoaded(ref.x(), ref.z())) {
				unloadChunk(world.getChunkAt(ref.x(), ref.z()));
			}
		}
	}

	public void reload() {
		stop();
		settings = FireSettings.load(plugin.getConfig());
		start();
	}

	public FireSettings getSettings() {
		return settings;
	}

	public long getTick() {
		return tick;
	}

	public AshManager getAsh() {
		return ash;
	}

	public Backdraft getBackdraft() {
		return backdraft;
	}

	public CollapseManager getCollapse() {
		return collapse;
	}

	public boolean isWorldEnabled(World world) {
		return settings.isWorldEnabled(world);
	}

	public BurningBlock get(BlockKey key) {
		return burning.get(key);
	}

	public Collection<BurningBlock> all() {
		return Collections.unmodifiableCollection(burning.values());
	}

	public boolean isBurningAt(BlockKey key) {
		final BurningBlock burningBlock = burning.get(key);
		return burningBlock != null && burningBlock.isBurning();
	}

	public boolean igniteAround(Block origin) {
		boolean ignited = ignite(origin);

		for(BlockFace face : FireBlocks.FACES) {
			final Block neighbor = FireBlocks.neighbor(origin, face);
			if(neighbor != null) {
				ignited |= ignite(neighbor);
			}
		}

		return ignited;
	}

	public boolean ignite(Block block) {
		return ignite(block, settings.heat().ignition());
	}

	public boolean ignite(Block block, double heat) {
		if(!isWorldEnabled(block.getWorld())) {
			return false;
		}

		final FireCategory category = settings.categoryOf(block.getType());
		if(category == null) {
			return false;
		}

		final BlockKey key = BlockKey.of(block);
		final BurningBlock existing = burning.get(key);
		if(existing != null) {
			existing.setHeat(Math.min(category.maxHeat(), Math.max(existing.getHeat(), heat)));
			if(!existing.isBurning()) {
				setState(existing, FireState.BURNING);
				ensureFlames(block);
			}
			return true;
		}

		if(burning.size() >= settings.maxBurningBlocks()) {
			return false;
		}

		final double burnTicks = category.burnTicks() * ThreadLocalRandom.current().nextDouble(0.8, 1.2);
		register(new BurningBlock(key, category, burnTicks, Math.min(category.maxHeat(), heat)));
		ensureFlames(block);
		return true;
	}

	public void restore(Block block, double remainingTicks, double heat, boolean smouldering) {
		final FireCategory category = settings.categoryOf(block.getType());
		if(category == null || burning.size() >= settings.maxBurningBlocks()) {
			return;
		}

		final BurningBlock burningBlock = new BurningBlock(BlockKey.of(block), category, remainingTicks, heat);
		register(burningBlock);
		if(smouldering) {
			burningBlock.setState(FireState.SMOULDERING);
		}
	}

	public BurningBlock unregister(BlockKey key) {
		final BurningBlock removed = burning.remove(key);

		if(removed != null) {
			final ChunkRef ref = key.chunk();
			final Set<BlockKey> keys = byChunk.get(ref);
			if(keys != null) {
				keys.remove(key);
				if(keys.isEmpty()) {
					byChunk.remove(ref);
				}
			}
		}

		return removed;
	}

	public void extinguish(BurningBlock burningBlock) {
		unregister(burningBlock.getKey());

		final Block block = burningBlock.getKey().toBlock();
		if(block != null) {
			removeFlames(block);
		}
	}

	public int clear(BlockKey center, int radius) {
		int cleared = 0;

		for(BurningBlock burningBlock : new ArrayList<>(burning.values())) {
			final BlockKey key = burningBlock.getKey();
			if(center == null || (key.world().equals(center.world()) && key.distanceSquared(center.x() + 0.5, center.y() + 0.5, center.z() + 0.5) <= (double) radius * radius)) {
				extinguish(burningBlock);
				cleared++;
			}
		}

		return cleared;
	}

	public boolean applyWater(Block center) {
		if(!isWorldEnabled(center.getWorld())) {
			return false;
		}

		final double cooling = settings.water().hoseCooling();
		boolean hit = false;

		for(BurningBlock burningBlock : burningWithin(center.getWorld().getUID(), center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 1.5)) {
			burningBlock.setHeat(Math.max(0, burningBlock.getHeat() - cooling));
			cool(burningBlock, burningBlock.getHeat() < settings.heat().extinguishThreshold());
			hit = true;
		}

		if(hit) {
			center.getWorld().playSound(center.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1f);
		}
		return true;
	}

	public boolean applyPowder(Block center) {
		if(!isWorldEnabled(center.getWorld())) {
			return false;
		}

		final FireSettings.Water water = settings.water();

		for(BurningBlock burningBlock : burningWithin(center.getWorld().getUID(), center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 1.5)) {
			burningBlock.setHeat(Math.max(0, burningBlock.getHeat() - water.powderCooling()));
			cool(burningBlock, burningBlock.getHeat() <= water.powderKnockdown());
		}

		return true;
	}

	private void cool(BurningBlock burningBlock, boolean knockDown) {
		if(!burningBlock.isBurning()) {
			burningBlock.setStateChangedAt(tick);
			return;
		}

		if(knockDown) {
			final Block block = burningBlock.getKey().toBlock();
			setState(burningBlock, FireState.SMOULDERING);
			if(block != null) {
				removeFlames(block);
			}
		}
	}

	public boolean isShatterable(Block block) {
		return isWorldEnabled(block.getWorld()) && burster.isShatterable(block);
	}

	public void shatter(Block block) {
		burster.burst(block, settings.weakBlocks().radius());
	}

	public void loadChunk(Chunk chunk) {
		for(FireStorage.StoredFire stored : storage.load(chunk)) {
			final Block block = chunk.getWorld().getBlockAt(stored.key().x(), stored.key().y(), stored.key().z());
			restore(block, stored.remainingTicks(), stored.heat(), stored.smouldering());
		}
	}

	public void unloadChunk(Chunk chunk) {
		ash.unloadChunk(chunk);
		hotSpots.remove(ChunkRef.of(chunk));

		final Set<BlockKey> keys = byChunk.get(ChunkRef.of(chunk));
		final List<BurningBlock> blocks = new ArrayList<>();

		if(keys != null) {
			for(BlockKey key : new ArrayList<>(keys)) {
				blocks.add(unregister(key));
			}
		}

		if(!blocks.isEmpty()) {
			storage.save(chunk, blocks);
		}
	}

	public boolean supportsFlame(Block fireBlock) {
		final BlockKey key = BlockKey.of(fireBlock);

		for(BlockFace face : FireBlocks.FACES) {
			if(isBurningAt(key.relative(face.getModX(), face.getModY(), face.getModZ()))) {
				return true;
			}
		}

		return false;
	}

	public double heatAround(UUID world, double x, double y, double z, double radius, boolean weighted) {
		final double radiusSquared = radius * radius;
		double total = 0;

		for(BurningBlock burningBlock : burningWithin(world, x, y, z, radius)) {
			final double share = burningBlock.isBurning() ? 1 : FireDefaults.SMOULDERING_HEAT_SHARE;
			final double distanceSquared = burningBlock.getKey().distanceSquared(x, y, z);
			total += share * (weighted ? burningBlock.getHeat() / Math.max(1, distanceSquared) : burningBlock.getHeat());
		}

		for(int cx = ((int) Math.floor(x - radius)) >> 4; cx <= ((int) Math.floor(x + radius)) >> 4; cx++) {
			for(int cz = ((int) Math.floor(z - radius)) >> 4; cz <= ((int) Math.floor(z + radius)) >> 4; cz++) {
				final Map<BlockKey, HotSpot> spots = hotSpots.get(new ChunkRef(world, cx, cz));
				if(spots == null) {
					continue;
				}
				for(Map.Entry<BlockKey, HotSpot> spot : spots.entrySet()) {
					final double distanceSquared = spot.getKey().distanceSquared(x, y, z);
					if(distanceSquared <= radiusSquared) {
						total += FireDefaults.HOT_SPOT_HEAT_SHARE * (weighted ? spot.getValue().heat / Math.max(1, distanceSquared) : spot.getValue().heat);
					}
				}
			}
		}

		return total;
	}

	public boolean tryAutoIgnite(Block block) {
		if(!isWorldEnabled(block.getWorld()) || settings.categoryOf(block.getType()) == null || burning.containsKey(BlockKey.of(block)) || !hasAirFace(block)) {
			return false;
		}

		final double heat = heatAround(block.getWorld().getUID(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, FireDefaults.AUTOIGNITION_RADIUS, true);
		return heat >= FireDefaults.AUTOIGNITION_HEAT && ignite(block);
	}

	public List<BurningBlock> burningWithin(UUID world, double x, double y, double z, double radius) {
		final List<BurningBlock> found = new ArrayList<>();
		final double radiusSquared = radius * radius;

		for(int cx = ((int) Math.floor(x - radius)) >> 4; cx <= ((int) Math.floor(x + radius)) >> 4; cx++) {
			for(int cz = ((int) Math.floor(z - radius)) >> 4; cz <= ((int) Math.floor(z + radius)) >> 4; cz++) {
				final Set<BlockKey> keys = byChunk.get(new ChunkRef(world, cx, cz));
				if(keys == null) {
					continue;
				}
				for(BlockKey key : keys) {
					if(key.distanceSquared(x, y, z) <= radiusSquared) {
						found.add(burning.get(key));
					}
				}
			}
		}

		return found;
	}

	public void invalidateEnvironment(BlockKey center, int radius) {
		for(BurningBlock burningBlock : burningWithin(center.world(), center.x() + 0.5, center.y() + 0.5, center.z() + 0.5, radius)) {
			burningBlock.setEnvironment(null);
		}
	}

	private void register(BurningBlock burningBlock) {
		burningBlock.setLastUpdate(tick);
		burningBlock.setStateChangedAt(tick);
		burning.put(burningBlock.getKey(), burningBlock);
		byChunk.computeIfAbsent(burningBlock.getKey().chunk(), ref -> new HashSet<>()).add(burningBlock.getKey());
	}

	private void setState(BurningBlock burningBlock, FireState state) {
		burningBlock.setState(state);
		burningBlock.setStateChangedAt(tick);
	}

	private void cycle() {
		tick += settings.updateInterval();

		if(tick % 1200 < settings.updateInterval()) {
			flashovers.values().removeIf(at -> tick - at >= settings.confinement().flashoverCooldown());
		}

		if(pending.isEmpty()) {
			pending.addAll(burning.keySet());
		}

		particleBudget = settings.ash().particles() ? settings.ash().maxParticlesPerCycle() : 0;
		particleBudget -= ash.tickParticles(tick, particleBudget);

		int budget = settings.maxUpdatesPerCycle();
		while(budget > 0 && !pending.isEmpty()) {
			final BurningBlock burningBlock = burning.get(pending.poll());
			if(burningBlock != null) {
				update(burningBlock);
				budget--;
			}
		}

		heatAura.tick();
		coolHotSpots();
		expireEphemeralFlames();
	}

	private void update(BurningBlock burningBlock) {
		final BlockKey key = burningBlock.getKey();
		final Block block = key.toBlock();
		final double seconds = (tick - burningBlock.getLastUpdate()) / 20.0;
		burningBlock.setLastUpdate(tick);

		if(block == null) {
			unregister(key);
			return;
		}
		if(!isWorldEnabled(block.getWorld()) || settings.categoryOf(block.getType()) == null) {
			extinguish(burningBlock);
			return;
		}

		int airFaces = 0;
		int waterFaces = isWaterlogged(block.getBlockData()) ? 2 : 0;
		int burningNeighbors = 0;

		for(BlockFace face : FireBlocks.FACES) {
			final Block neighbor = FireBlocks.neighbor(block, face);
			if(neighbor == null) {
				continue;
			}

			final Material type = neighbor.getType();
			if(type.isAir() || type == Material.FIRE) {
				airFaces++;
			}
			else if(type == Material.WATER || isWaterlogged(neighbor.getBlockData())) {
				waterFaces++;
			}

			if(isBurningAt(BlockKey.of(neighbor))) {
				burningNeighbors++;
			}
		}

		final FireSettings.Heat heatSettings = settings.heat();
		final FireSettings.Confinement confinement = settings.confinement();
		final FireEnvironment.Environment environment = FireEnvironment.of(burningBlock, block, tick, confinement.maxRoomVolume());
		final double oxygen = oxygen(environment, airFaces);
		final double dissipation = dissipation(environment);
		final double rain = environment.openAir() && block.getWorld().hasStorm() ? settings.openAir().rainCooling() : 0;
		final double cooling = waterFaces * settings.water().adjacentWaterCooling() + rain;
		final double maxHeat = burningBlock.getCategory().maxHeat();

		if(burningBlock.isBurning()) {
			final double multiplier = environment.confined() ? confinement.heatMultiplier() : 1;
			final double growth = heatSettings.growth() * oxygen * multiplier * (1 + burningNeighbors * settings.spread().heatPerNeighbor());
			burningBlock.setHeat(clamp(burningBlock.getHeat() + (growth - dissipation - cooling) * seconds, maxHeat));

			if(burningBlock.getHeat() < heatSettings.extinguishThreshold()) {
				smoulder(burningBlock, block);
				return;
			}

			final double burnSpeed = environment.openAir() ? settings.openAir().burnSpeed() : environment.confined() ? confinement.burnSpeed() : 1;
			burningBlock.setRemainingTicks(burningBlock.getRemainingTicks() - seconds * 20 * burnSpeed * Math.max(0.25, burningBlock.heatRatio()));
			if(burningBlock.getRemainingTicks() <= 0) {
				burnOut(burningBlock, block);
				return;
			}

			spread(burningBlock, block, seconds, burningNeighbors, oxygen);
			scorch(burningBlock, block, seconds);
			if(burningBlock.heatRatio() >= 0.5 && ThreadLocalRandom.current().nextInt(20) == 0) {
				burster.scanAround(block);
				if(!burning.containsKey(key)) {
					return;
				}
			}
			if(confinement.flashover() && environment.confined() && burningBlock.heatRatio() >= 0.8 && ThreadLocalRandom.current().nextInt(10) == 0) {
				flashover(block);
			}
			ensureFlames(block);
			burningEffects(burningBlock, block, environment, burningNeighbors);
		}
		else {
			final double radiant = burningNeighbors * heatSettings.growth() * settings.spread().heatPerNeighbor() * oxygen;
			burningBlock.setHeat(clamp(burningBlock.getHeat() + (radiant - dissipation - cooling) * seconds, maxHeat));

			if(burningBlock.getHeat() <= 0) {
				unregister(key);
				return;
			}

			if(oxygen > 0 && burningBlock.getHeat() >= heatSettings.reigniteThreshold() && tick - burningBlock.getStateChangedAt() >= heatSettings.reigniteDelay()) {
				setState(burningBlock, FireState.BURNING);
				ensureFlames(block);
			}
			else {
				smoulderEffects(block);
			}
		}
	}

	private double oxygen(FireEnvironment.Environment environment, int airFaces) {
		if(airFaces == 0) {
			return 0;
		}
		if(environment.openAir()) {
			return 1;
		}
		if(environment.confined()) {
			return 0.4 + 0.4 * Math.min(1, environment.roomVolume() / (double) Math.max(1, settings.confinement().maxRoomVolume()));
		}
		return 0.8;
	}

	private void spread(BurningBlock burningBlock, Block block, double seconds, int burningNeighbors, double oxygen) {
		final FireSettings.Spread spread = settings.spread();
		final double base = spread.baseChance() * burningBlock.heatRatio() * (1 + burningNeighbors * spread.heatPerNeighbor()) * oxygen * seconds;
		if(base <= 0) {
			return;
		}

		final World world = block.getWorld();
		final int radius = spread.radius();
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		for(int dx = -radius; dx <= radius; dx++) {
			for(int dy = -radius; dy <= radius; dy++) {
				for(int dz = -radius; dz <= radius; dz++) {
					final int distanceSquared = dx * dx + dy * dy + dz * dz;
					if(distanceSquared == 0) {
						continue;
					}

					final Block target = FireBlocks.blockAt(world, block.getX() + dx, block.getY() + dy, block.getZ() + dz);
					if(target == null) {
						continue;
					}

					final double vertical = dy > 0 ? spread.upwardBonus() : dy < 0 ? 0.5 : 1;
					if(target.getType() == Material.TNT) {
						if(random.nextDouble() < base * vertical / distanceSquared) {
							primeTnt(target);
						}
						continue;
					}

					final FireCategory category = settings.categoryOf(target.getType());
					if(category == null || burning.containsKey(BlockKey.of(target))) {
						continue;
					}

					if(random.nextDouble() < base * category.ignitionChance() * vertical / distanceSquared && hasAirFace(target)) {
						ignite(target);
					}
				}
			}
		}
	}

	private void scorch(BurningBlock burningBlock, Block block, double seconds) {
		final FireSettings.Scorch scorch = settings.scorch();
		final ThreadLocalRandom random = ThreadLocalRandom.current();
		if(!scorch.enabled() || burningBlock.heatRatio() < FireDefaults.SCORCH_HEAT_RATIO) {
			return;
		}

		final int area = (2 * FireDefaults.SCORCH_RADIUS + 1) * (2 * FireDefaults.SCORCH_RADIUS + 1);
		final int attempts = (int) Math.floor(scorch.chancePerSecond() * seconds * area + random.nextDouble());
		for(int i = 0; i < attempts; i++) {
			final Block ground = FireBlocks.blockAt(block.getWorld(),
					block.getX() + random.nextInt(-FireDefaults.SCORCH_RADIUS, FireDefaults.SCORCH_RADIUS + 1),
					block.getY() - random.nextInt(0, FireDefaults.SCORCH_DEPTH + 1),
					block.getZ() + random.nextInt(-FireDefaults.SCORCH_RADIUS, FireDefaults.SCORCH_RADIUS + 1));
			if(ground != null && scorch.convertible().contains(ground.getType())) {
				ground.setType(ground.getType() == Material.DIRT ? Material.COARSE_DIRT : scorch.materials().pick());
			}
		}
	}

	public void primeTnt(Block block) {
		final World world = block.getWorld();
		if(!settings.features().tnt() || Boolean.FALSE.equals(world.getGameRuleValue(GameRule.TNT_EXPLODES))) {
			return;
		}

		block.setType(Material.AIR);
		world.spawn(block.getLocation().add(0.5, 0, 0.5), TNTPrimed.class, tnt -> tnt.setFuseTicks(FireDefaults.TNT_FUSE_TICKS));
	}

	private void flashover(Block block) {
		final FireSettings.Confinement confinement = settings.confinement();
		final Location center = block.getLocation().add(0.5, 0.5, 0.5);
		final int radius = confinement.flashoverRadius();
		if(isZoneCoolingDown(block) || heatAround(block.getWorld().getUID(), center.getX(), center.getY(), center.getZ(), radius, false) < confinement.flashoverHeat()) {
			return;
		}
		claimZone(block);
		engulf(block, radius);

		final World world = block.getWorld();
		world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, FireDefaults.FLASHOVER_VOLUME, 0.5f);
		world.playSound(center, Sound.BLOCK_FIRE_AMBIENT, FireDefaults.FLASHOVER_VOLUME, 0.6f);
		world.spawnParticle(Particle.FLAME, center, 150, radius / 2.0, radius / 2.0, radius / 2.0, 0.05);
		_log.debug("[flashover] Flashover at {} {} {}", block.getX(), block.getY(), block.getZ());
	}

	public boolean isZoneCoolingDown(Block block) {
		final Long last = flashovers.get(zone(block));
		return last != null && tick - last < settings.confinement().flashoverCooldown();
	}

	public void claimZone(Block block) {
		flashovers.put(zone(block), tick);
	}

	private static BlockKey zone(Block block) {
		return new BlockKey(block.getWorld().getUID(), block.getX() >> 3, block.getY() >> 3, block.getZ() >> 3);
	}

	public void engulf(Block center, int radius) {
		final FireSettings.Confinement confinement = settings.confinement();
		final ThreadLocalRandom random = ThreadLocalRandom.current();
		final World world = center.getWorld();

		for(int dx = -radius; dx <= radius; dx++) {
			for(int dy = -radius; dy <= radius; dy++) {
				for(int dz = -radius; dz <= radius; dz++) {
					final Block target = FireBlocks.blockAt(world, center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					if(target == null) {
						continue;
					}
					if(target.getType() == Material.TNT) {
						primeTnt(target);
						continue;
					}
					if(target.getType().isAir()) {
						if(random.nextDouble() < FireDefaults.EPHEMERAL_FLAME_CHANCE) {
							placeEphemeralFlame(target);
						}
						continue;
					}

					final FireCategory category = settings.categoryOf(target.getType());
					if(category != null && hasAirFace(target) && random.nextDouble() < confinement.flashoverIgniteChance()) {
						ignite(target, category.maxHeat() * confinement.flashoverHeatRatio());
					}
				}
			}
		}
	}

	public void placeEphemeralFlame(Block cell) {
		if(!cell.getType().isAir()) {
			return;
		}

		final Fire flame = (Fire) Material.FIRE.createBlockData();
		final Block below = FireBlocks.neighbor(cell, BlockFace.DOWN);
		if(below == null || !below.getType().isSolid()) {
			BlockFace attach = null;
			for(BlockFace face : flame.getAllowedFaces()) {
				final Block side = FireBlocks.neighbor(cell, face);
				if(side != null && side.getType().isSolid()) {
					attach = face;
					break;
				}
			}
			if(attach == null) {
				return;
			}
			flame.setFace(attach, true);
		}

		cell.setBlockData(flame, false);
		final long lifetime = ThreadLocalRandom.current().nextLong(FireDefaults.EPHEMERAL_FLAME_MIN_TICKS, FireDefaults.EPHEMERAL_FLAME_MAX_TICKS + 1);
		ephemeralFlames.put(BlockKey.of(cell), tick + lifetime);
	}

	private void expireEphemeralFlames() {
		ephemeralFlames.entrySet().removeIf(entry -> {
			if(entry.getValue() > tick) {
				return false;
			}
			final Block block = entry.getKey().toBlock();
			if(block != null && block.getType() == Material.FIRE && !supportsFlame(block)) {
				block.setType(Material.AIR, false);
			}
			return true;
		});
	}

	private static boolean hasAirFace(Block block) {
		for(BlockFace face : FireBlocks.FACES) {
			final Block neighbor = FireBlocks.neighbor(block, face);
			if(neighbor != null && (neighbor.getType().isAir() || neighbor.getType() == Material.FIRE)) {
				return true;
			}
		}
		return false;
	}

	private void burningEffects(BurningBlock burningBlock, Block block, FireEnvironment.Environment environment, int burningNeighbors) {
		if(particleBudget <= 0) {
			return;
		}

		final World world = block.getWorld();
		final Location top = block.getLocation().add(0.5, 1.2, 0.5);
		final ThreadLocalRandom random = ThreadLocalRandom.current();
		final double ratio = burningBlock.heatRatio();
		final double density = settings.ash().particleDensity() * (1 + burningNeighbors * 0.5);

		particleBudget -= spawn(world, Particle.WHITE_ASH, top, 4 * ratio * density, 1.5, 1.5, 1.5, 0);
		particleBudget -= spawn(world, Particle.LARGE_SMOKE, top, 1.5 * ratio * density, 0.3, 0.5, 0.3, 0.03);
		particleBudget -= spawn(world, Particle.ASH, top, 2 * ratio * density, 1.2, 1.2, 1.2, 0);

		if(ratio >= FireDefaults.INTENSE_FIRE_RATIO) {
			final Location center = block.getLocation().add(0.5, 0.6, 0.5);
			particleBudget -= spawn(world, Particle.FLAME, center, 4 * ratio * density, 0.5, 0.6, 0.5, 0.02);
			particleBudget -= spawn(world, Particle.LAVA, top, 0.6 * ratio * density, 0.3, 0.2, 0.3, 0);
			particleBudget -= spawn(world, Particle.SMOKE, top, 3 * ratio * density, 0.4, 0.6, 0.4, 0.04);
		}
		if(ratio > 0.7 && random.nextDouble() < (environment.openAir() ? 0.25 : 0.1) * density) {
			world.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, top, 0, 0, 0.08, 0);
			particleBudget--;
		}
		if(random.nextDouble() < 0.05 + 0.1 * ratio) {
			world.playSound(top, Sound.BLOCK_FIRE_AMBIENT, 0.8f + (float) ratio, 0.8f + random.nextFloat() * 0.4f);
		}
	}

	private static int spawn(World world, Particle particle, Location location, double amount, double dx, double dy, double dz, double speed) {
		final int count = (int) Math.floor(amount + ThreadLocalRandom.current().nextDouble());
		if(count > 0) {
			world.spawnParticle(particle, location, count, dx, dy, dz, speed);
		}
		return count;
	}

	private void smoulderEffects(Block block) {
		if(particleBudget <= 0 || ThreadLocalRandom.current().nextDouble() >= 0.3) {
			return;
		}

		block.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, block.getLocation().add(0.5, 1, 0.5), 0, 0, 0.03, 0);
		particleBudget--;
	}

	private void smoulder(BurningBlock burningBlock, Block block) {
		setState(burningBlock, FireState.SMOULDERING);
		removeFlames(block);
	}

	private void burnOut(BurningBlock burningBlock, Block block) {
		unregister(burningBlock.getKey());
		removeFlames(block);
		block.setType(Material.AIR);
		collapse.collapseAround(block);
		invalidateEnvironment(burningBlock.getKey(), 3);

		if(burningBlock.getCategory().ash() && ThreadLocalRandom.current().nextDouble() < settings.ash().chance()) {
			ash.spawn(block);
		}

		final FireEnvironment.Environment environment = burningBlock.getEnvironment();
		if(burningBlock.getHeat() > 0) {
			final HotSpot spot = new HotSpot(burningBlock.getHeat(), environment == null ? settings.heat().dissipation() : dissipation(environment));
			hotSpots.computeIfAbsent(burningBlock.getKey().chunk(), ref -> new HashMap<>()).merge(burningBlock.getKey(), spot, (a, b) -> a.heat >= b.heat ? a : b);
		}
	}

	private double dissipation(FireEnvironment.Environment environment) {
		if(environment.openAir()) {
			return settings.openAir().heatDissipation();
		}
		return environment.confined() ? settings.confinement().dissipation() : settings.heat().dissipation();
	}

	private static final class HotSpot {
		private double heat;
		private final double rate;

		private HotSpot(double heat, double rate) {
			this.heat = heat;
			this.rate = rate;
		}
	}

	private void coolHotSpots() {
		final double seconds = settings.updateInterval() / 20.0;
		final ThreadLocalRandom random = ThreadLocalRandom.current();
		final List<Block> candidates = new ArrayList<>();

		hotSpots.values().removeIf(spots -> {
			spots.entrySet().removeIf(spot -> {
				spot.getValue().heat -= spot.getValue().rate * seconds;
				if(spot.getValue().heat <= 0) {
					return true;
				}

				final Block block = spot.getKey().toBlock();
				if(block != null && random.nextDouble() < FireDefaults.HOT_SPOT_IGNITE_CHANCE) {
					for(BlockFace face : FireBlocks.FACES) {
						final Block neighbor = FireBlocks.neighbor(block, face);
						if(neighbor != null && settings.categoryOf(neighbor.getType()) != null) {
							candidates.add(neighbor);
						}
					}
				}
				return false;
			});
			return spots.isEmpty();
		});

		candidates.forEach(this::tryAutoIgnite);
	}

	private void ensureFlames(Block block) {
		for(BlockFace face : FireBlocks.FACES) {
			final Block neighbor = FireBlocks.neighbor(block, face);
			if(neighbor == null || !neighbor.getType().isAir()) {
				continue;
			}

			final Fire fire = (Fire) Material.FIRE.createBlockData();
			final Block below = FireBlocks.neighbor(neighbor, BlockFace.DOWN);
			final boolean grounded = face == BlockFace.UP || (below != null && below.getType().isSolid());

			if(!grounded) {
				final BlockFace attach = face.getOppositeFace();
				if(!fire.getAllowedFaces().contains(attach)) {
					continue;
				}
				fire.setFace(attach, true);
			}

			neighbor.setBlockData(fire, false);
		}
	}

	private void removeFlames(Block block) {
		for(BlockFace face : FireBlocks.FACES) {
			final Block neighbor = FireBlocks.neighbor(block, face);
			if(neighbor != null && neighbor.getType() == Material.FIRE && !supportsFlame(neighbor)) {
				neighbor.setType(Material.AIR, false);
			}
		}
	}

	private static boolean isWaterlogged(BlockData data) {
		return data instanceof Waterlogged waterlogged && waterlogged.isWaterlogged();
	}

	private static double clamp(double value, double max) {
		return Math.max(0, Math.min(max, value));
	}
}

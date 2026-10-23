package fr.stan1712.wetston.fireequipment.fire;

import fr.stan1712.wetston.fireequipment.Main;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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

	private final Main plugin;
	private final FireStorage storage;
	@Getter
	private final AshManager ash;
	@Getter
	private final CollapseManager collapse;
	private final HeatAura heatAura;
	private final WeakBlockBurster burster;
	private final Map<BlockKey, BurningBlock> burning = new HashMap<>();
	private final Map<ChunkRef, Set<BlockKey>> byChunk = new HashMap<>();
	private final ArrayDeque<BlockKey> pending = new ArrayDeque<>();
	private final Map<BlockKey, Long> flashovers = new HashMap<>();

	@Getter
	private FireSettings settings;
	@Getter
	private long tick;
	private BukkitTask task;
	private int particleBudget;

	public FireManager(Main plugin) {
		this.plugin = plugin;
		this.storage = new FireStorage(plugin);
		this.ash = new AshManager(plugin, this);
		this.collapse = new CollapseManager(plugin, this);
		this.heatAura = new HeatAura(this);
		this.burster = new WeakBlockBurster(this);
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

		if(center.getType() == Material.FIRE && !supportsFlame(center)) {
			center.setType(Material.AIR);
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

	public void loadChunk(Chunk chunk) {
		for(FireStorage.StoredFire stored : storage.load(chunk)) {
			final Block block = chunk.getWorld().getBlockAt(stored.key().x(), stored.key().y(), stored.key().z());
			restore(block, stored.remainingTicks(), stored.heat(), stored.smouldering());
		}
	}

	public void unloadChunk(Chunk chunk) {
		ash.unloadChunk(chunk);

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
			if(burningBlock.isBurning()) {
				final double distanceSquared = burningBlock.getKey().distanceSquared(x, y, z);
				if(distanceSquared <= radiusSquared) {
					total += weighted ? burningBlock.getHeat() / Math.max(1, distanceSquared) : burningBlock.getHeat();
				}
			}
		}

		return total;
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
		final double dissipation = environment.openAir() ? settings.openAir().heatDissipation() : heatSettings.dissipation();
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
			if(burningBlock.heatRatio() >= 0.5 && ThreadLocalRandom.current().nextInt(20) == 0) {
				burster.scanAround(block);
				if(!burning.containsKey(key)) {
					return;
				}
			}
			if(environment.confined() && burningBlock.heatRatio() >= 0.8 && ThreadLocalRandom.current().nextInt(10) == 0) {
				flashover(block);
			}
			ensureFlames(block);
			burningEffects(burningBlock, block, environment);
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

					final FireCategory category = settings.categoryOf(target.getType());
					if(category == null || burning.containsKey(BlockKey.of(target))) {
						continue;
					}

					final double vertical = dy > 0 ? spread.upwardBonus() : dy < 0 ? 0.5 : 1;
					if(random.nextDouble() < base * category.ignitionChance() * vertical / distanceSquared && hasAirFace(target)) {
						ignite(target);
					}
				}
			}
		}
	}

	private void flashover(Block block) {
		final FireSettings.Confinement confinement = settings.confinement();
		final BlockKey zone = new BlockKey(block.getWorld().getUID(), block.getX() >> 3, block.getY() >> 3, block.getZ() >> 3);
		final Long last = flashovers.get(zone);
		if(last != null && tick - last < confinement.flashoverCooldown()) {
			return;
		}

		final Location center = block.getLocation().add(0.5, 0.5, 0.5);
		final int radius = confinement.flashoverRadius();
		if(heatAround(block.getWorld().getUID(), center.getX(), center.getY(), center.getZ(), radius, false) < confinement.flashoverHeat()) {
			return;
		}
		flashovers.put(zone, tick);

		final World world = block.getWorld();
		for(int dx = -radius; dx <= radius; dx++) {
			for(int dy = -radius; dy <= radius; dy++) {
				for(int dz = -radius; dz <= radius; dz++) {
					final Block target = FireBlocks.blockAt(world, block.getX() + dx, block.getY() + dy, block.getZ() + dz);
					final FireCategory category = target == null ? null : settings.categoryOf(target.getType());
					if(category != null && hasAirFace(target)) {
						ignite(target, category.maxHeat() * 0.8);
					}
				}
			}
		}

		world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);
		world.playSound(center, Sound.BLOCK_FIRE_AMBIENT, 2f, 0.6f);
		world.spawnParticle(Particle.FLAME, center, 150, radius / 2.0, radius / 2.0, radius / 2.0, 0.05);
		_log.debug("[flashover] Flashover at {} {} {}", block.getX(), block.getY(), block.getZ());
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

	private void burningEffects(BurningBlock burningBlock, Block block, FireEnvironment.Environment environment) {
		if(particleBudget <= 0) {
			return;
		}

		final World world = block.getWorld();
		final Location top = block.getLocation().add(0.5, 1.2, 0.5);
		final ThreadLocalRandom random = ThreadLocalRandom.current();
		final int whiteAsh = (int) Math.ceil(4 * burningBlock.heatRatio());

		world.spawnParticle(Particle.WHITE_ASH, top, whiteAsh, 1.5, 1.5, 1.5, 0);
		particleBudget -= whiteAsh;

		if(random.nextDouble() < 0.4) {
			world.spawnParticle(Particle.LARGE_SMOKE, top, 1, 0.2, 0.3, 0.2, 0.02);
			particleBudget--;
		}
		if(random.nextDouble() < 0.1) {
			world.spawnParticle(Particle.ASH, top, 4, 1, 1, 1, 0);
			particleBudget -= 4;
		}
		if(environment.openAir() && burningBlock.heatRatio() > 0.7 && random.nextDouble() < 0.08) {
			world.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, top, 0, 0, 0.08, 0);
			particleBudget--;
		}
		if(random.nextDouble() < 0.05) {
			world.playSound(top, Sound.BLOCK_FIRE_AMBIENT, 0.8f, 0.8f + random.nextFloat() * 0.4f);
		}
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
		collapse.collapseAbove(block);
		invalidateEnvironment(burningBlock.getKey(), 3);

		if(burningBlock.getCategory().ash() && ThreadLocalRandom.current().nextDouble() < settings.ash().chance()) {
			ash.spawn(block);
		}
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

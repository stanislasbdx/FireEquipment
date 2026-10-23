package fr.stan1712.wetston.fireequipment.fire;

import fr.stan1712.wetston.fireequipment.Main;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
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
import java.util.concurrent.ThreadLocalRandom;

public class FireManager {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Fire");

	private final Main plugin;
	private final FireStorage storage;
	private final Map<BlockKey, BurningBlock> burning = new HashMap<>();
	private final Map<ChunkRef, Set<BlockKey>> byChunk = new HashMap<>();
	private final ArrayDeque<BlockKey> pending = new ArrayDeque<>();

	@Getter
	private FireSettings settings;
	@Getter
	private long tick;
	private BukkitTask task;

	public FireManager(Main plugin) {
		this.plugin = plugin;
		this.storage = new FireStorage(plugin);
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

	public void loadChunk(Chunk chunk) {
		for(FireStorage.StoredFire stored : storage.load(chunk)) {
			final Block block = chunk.getWorld().getBlockAt(stored.key().x(), stored.key().y(), stored.key().z());
			restore(block, stored.remainingTicks(), stored.heat(), stored.smouldering());
		}
	}

	public void unloadChunk(Chunk chunk) {
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

		if(pending.isEmpty()) {
			pending.addAll(burning.keySet());
		}

		int budget = settings.maxUpdatesPerCycle();
		while(budget > 0 && !pending.isEmpty()) {
			final BurningBlock burningBlock = burning.get(pending.poll());
			if(burningBlock != null) {
				update(burningBlock);
				budget--;
			}
		}
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
		final double oxygen = airFaces > 0 ? 1 : 0;
		final double cooling = waterFaces * settings.water().adjacentWaterCooling();
		final double maxHeat = burningBlock.getCategory().maxHeat();

		if(burningBlock.isBurning()) {
			final double growth = heatSettings.growth() * oxygen * (1 + burningNeighbors * settings.spread().heatPerNeighbor());
			burningBlock.setHeat(clamp(burningBlock.getHeat() + (growth - heatSettings.dissipation() - cooling) * seconds, maxHeat));

			if(burningBlock.getHeat() < heatSettings.extinguishThreshold()) {
				smoulder(burningBlock, block);
				return;
			}

			burningBlock.setRemainingTicks(burningBlock.getRemainingTicks() - seconds * 20 * Math.max(0.25, burningBlock.heatRatio()));
			if(burningBlock.getRemainingTicks() <= 0) {
				burnOut(burningBlock, block);
				return;
			}

			ensureFlames(block);
		}
		else {
			final double radiant = burningNeighbors * heatSettings.growth() * settings.spread().heatPerNeighbor() * oxygen;
			burningBlock.setHeat(clamp(burningBlock.getHeat() + (radiant - heatSettings.dissipation() - cooling) * seconds, maxHeat));

			if(burningBlock.getHeat() <= 0) {
				unregister(key);
				return;
			}

			if(oxygen > 0 && burningBlock.getHeat() >= heatSettings.reigniteThreshold() && tick - burningBlock.getStateChangedAt() >= heatSettings.reigniteDelay()) {
				setState(burningBlock, FireState.BURNING);
				ensureFlames(block);
			}
		}
	}

	private void smoulder(BurningBlock burningBlock, Block block) {
		setState(burningBlock, FireState.SMOULDERING);
		removeFlames(block);
	}

	private void burnOut(BurningBlock burningBlock, Block block) {
		unregister(burningBlock.getKey());
		removeFlames(block);
		block.setType(Material.AIR);
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

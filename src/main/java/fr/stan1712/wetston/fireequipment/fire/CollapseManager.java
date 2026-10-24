package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.FallingBlock;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollapseManager {
	private final Plugin plugin;
	private final FireManager fire;
	private final NamespacedKey burningKey;

	public CollapseManager(Plugin plugin, FireManager fire) {
		this.plugin = plugin;
		this.fire = fire;
		this.burningKey = new NamespacedKey(plugin, "burning");
	}

	public void collapseAround(Block removed) {
		final FireSettings.Collapse collapse = fire.getSettings().collapse();
		if(!collapse.enabled()) {
			return;
		}

		final Set<Long> checked = new HashSet<>();
		for(BlockFace face : FireBlocks.FACES) {
			final Block neighbor = FireBlocks.neighbor(removed, face);
			if(neighbor == null || !isStructural(neighbor) || checked.contains(BlockKey.of(neighbor).pack())) {
				continue;
			}

			final List<Block> piece = new ArrayList<>();
			if(!isAnchored(neighbor, piece, checked, collapse)) {
				drop(piece, collapse);
			}
		}
	}

	public void land(FallingBlock fallingBlock, Block block) {
		final String stored = fallingBlock.getPersistentDataContainer().get(burningKey, PersistentDataType.STRING);
		if(stored == null) {
			return;
		}

		final String[] parts = stored.split(";");
		final double remainingTicks = Double.parseDouble(parts[0]);
		final double heat = Double.parseDouble(parts[1]);
		final boolean smouldering = Boolean.parseBoolean(parts[2]);

		Bukkit.getScheduler().runTask(plugin, () -> fire.restore(block, remainingTicks, heat, smouldering));
	}

	private boolean isAnchored(Block start, List<Block> piece, Set<Long> checked, FireSettings.Collapse collapse) {
		final ArrayDeque<Block> queue = new ArrayDeque<>();
		final Set<Long> visited = new HashSet<>();
		final World world = start.getWorld();

		queue.add(start);
		visited.add(BlockKey.of(start).pack());

		try {
			while(!queue.isEmpty()) {
				final Block current = queue.poll();
				piece.add(current);

				if(piece.size() > FireDefaults.COLLAPSE_MAX_STRUCTURE || collapse.anchors().contains(current.getType()) || current.getY() <= world.getMinHeight()) {
					return true;
				}

				for(BlockFace face : FireBlocks.FACES) {
					final Block next = FireBlocks.neighbor(current, face);
					if(next == null) {
						return true;
					}
					if(isStructural(next) && visited.add(BlockKey.of(next).pack())) {
						queue.add(next);
					}
				}
			}
			return false;
		}
		finally {
			checked.addAll(visited);
		}
	}

	private static boolean isStructural(Block block) {
		final Material type = block.getType();
		return type.isSolid() && type != Material.FIRE;
	}

	private void drop(List<Block> piece, FireSettings.Collapse collapse) {
		piece.sort(Comparator.comparingInt(Block::getY));

		if(collapse.effects() && piece.size() >= FireDefaults.COLLAPSE_EFFECT_THRESHOLD) {
			effects(piece);
		}

		for(Block block : piece) {
			if(block.getType().isAir()) {
				continue;
			}

			final BlockData data = block.getBlockData();
			if(data instanceof Bisected || data instanceof Bed || block.getState() instanceof Container) {
				final BurningBlock burningBlock = fire.get(BlockKey.of(block));
				if(burningBlock != null) {
					fire.extinguish(burningBlock);
				}
				block.breakNaturally();
				continue;
			}

			fall(block, data);
		}
	}

	private void fall(Block block, BlockData data) {
		final Location location = block.getLocation().add(0.5, 0, 0.5);
		final BurningBlock burningBlock = fire.get(BlockKey.of(block));

		if(burningBlock != null) {
			fire.extinguish(burningBlock);
		}

		block.setType(Material.AIR);
		final FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(location, data);

		if(burningBlock != null) {
			fallingBlock.setVisualFire(true);
			fallingBlock.getPersistentDataContainer().set(burningKey, PersistentDataType.STRING,
					burningBlock.getRemainingTicks() + ";" + burningBlock.getHeat() + ";" + !burningBlock.isBurning());
		}
	}

	private void effects(List<Block> piece) {
		final World world = piece.get(0).getWorld();
		final Map<Material, Integer> counts = new EnumMap<>(Material.class);
		double x = 0;
		double y = 0;
		double z = 0;

		for(Block block : piece) {
			counts.merge(block.getType(), 1, Integer::sum);
			x += block.getX() + 0.5;
			y += block.getY() + 0.5;
			z += block.getZ() + 0.5;
		}

		final int size = piece.size();
		final Location center = new Location(world, x / size, y / size, z / size);
		final Material dominant = counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(Material.STONE);
		final BlockData dominantData = dominant.createBlockData();
		final double spread = Math.min(4, 0.5 + Math.cbrt(size));
		final float volume = (float) Math.min(4, 1 + size / 24.0);

		world.spawnParticle(Particle.BLOCK, center, Math.min(240, size * 8), spread, spread, spread, 0, dominantData);
		world.spawnParticle(Particle.LARGE_SMOKE, center, Math.min(80, size * 2), spread, spread * 0.6, spread, 0.03);
		world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, center, Math.min(30, size), spread, 0.5, spread, 0.02);
		world.spawnParticle(Particle.WHITE_ASH, center, Math.min(120, size * 4), spread * 1.5, spread, spread * 1.5, 0);
		world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, volume, 0.5f);
		world.playSound(center, dominantData.getSoundGroup().getBreakSound(), volume, 0.6f);
	}
}

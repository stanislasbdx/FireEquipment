package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class FireEnvironment {
	public static final int REFRESH_TICKS = 100;

	public record Environment(boolean openAir, boolean confined, int roomVolume) {}

	private FireEnvironment() {
		throw new IllegalStateException("Utility class");
	}

	public static Environment of(BurningBlock burningBlock, Block block, long tick, int maxRoomVolume) {
		if(burningBlock.getEnvironment() == null || tick - burningBlock.getEnvironmentAt() >= REFRESH_TICKS) {
			burningBlock.setEnvironment(compute(block, maxRoomVolume));
			burningBlock.setEnvironmentAt(tick);
		}
		return burningBlock.getEnvironment();
	}

	public static Environment compute(Block block, int maxRoomVolume) {
		final ArrayDeque<Block> queue = new ArrayDeque<>();
		final Set<Long> visited = new HashSet<>();

		for(BlockFace face : FireBlocks.FACES) {
			final Block neighbor = FireBlocks.neighbor(block, face);
			if(neighbor != null && isTraversable(neighbor)) {
				if(isExposed(neighbor)) {
					return new Environment(true, false, 0);
				}
				if(visited.add(BlockKey.of(neighbor).pack())) {
					queue.add(neighbor);
				}
			}
		}

		while(!queue.isEmpty()) {
			if(visited.size() > maxRoomVolume) {
				return new Environment(false, false, visited.size());
			}

			final Block current = queue.poll();
			if(isExposed(current)) {
				return new Environment(false, false, visited.size());
			}

			for(BlockFace face : FireBlocks.FACES) {
				final Block next = FireBlocks.neighbor(current, face);
				if(next != null && isTraversable(next) && visited.add(BlockKey.of(next).pack())) {
					queue.add(next);
				}
			}
		}

		return new Environment(false, true, visited.size());
	}

	public static boolean isTraversable(Block block) {
		final Material type = block.getType();
		return type.isAir() || type == Material.FIRE || (!type.isSolid() && type != Material.WATER && type != Material.LAVA);
	}

	public static boolean isExposed(Block block) {
		final World world = block.getWorld();
		return block.getY() > world.getHighestBlockYAt(block.getX(), block.getZ());
	}
}

package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class FireBlocks {
	private FireBlocks() {
		throw new IllegalStateException("Utility class");
	}

	public static final BlockFace[] FACES = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

	public static Block blockAt(World world, int x, int y, int z) {
		if(y < world.getMinHeight() || y >= world.getMaxHeight() || !world.isChunkLoaded(x >> 4, z >> 4)) {
			return null;
		}
		return world.getBlockAt(x, y, z);
	}

	public static Block neighbor(Block block, BlockFace face) {
		return blockAt(block.getWorld(), block.getX() + face.getModX(), block.getY() + face.getModY(), block.getZ() + face.getModZ());
	}
}

package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.UUID;

public record BlockKey(UUID world, int x, int y, int z) {
	public static BlockKey of(Block block) {
		return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
	}

	public static BlockKey unpack(UUID world, long packed) {
		return new BlockKey(world, (int) (packed >> 38), (int) (packed << 52 >> 52), (int) (packed << 26 >> 38));
	}

	public long pack() {
		return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
	}

	public BlockKey relative(int dx, int dy, int dz) {
		return new BlockKey(world, x + dx, y + dy, z + dz);
	}

	public ChunkRef chunk() {
		return new ChunkRef(world, x >> 4, z >> 4);
	}

	public double distanceSquared(double px, double py, double pz) {
		final double dx = x + 0.5 - px;
		final double dy = y + 0.5 - py;
		final double dz = z + 0.5 - pz;
		return dx * dx + dy * dy + dz * dz;
	}

	public Block toBlock() {
		final World w = Bukkit.getWorld(world);
		if(w == null || !w.isChunkLoaded(x >> 4, z >> 4)) {
			return null;
		}
		return w.getBlockAt(x, y, z);
	}
}

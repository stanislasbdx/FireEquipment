package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Chunk;

import java.util.UUID;

public record ChunkRef(UUID world, int x, int z) {
	public static ChunkRef of(Chunk chunk) {
		return new ChunkRef(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
	}
}

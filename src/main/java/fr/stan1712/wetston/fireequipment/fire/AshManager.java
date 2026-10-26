package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AshManager {
	private final FireManager fire;
	private final NamespacedKey ashKey;
	private final Map<ChunkRef, Set<Long>> positions = new HashMap<>();
	private final Map<BlockKey, Long> fresh = new HashMap<>();

	public AshManager(Plugin plugin, FireManager fire) {
		this.fire = fire;
		this.ashKey = new NamespacedKey(plugin, "ash");
	}

	public void spawn(Block block) {
		final Material material = fire.getSettings().ash().pick();
		tag(block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0, 0.5), material.createBlockData()));
	}

	public void tag(FallingBlock fallingBlock) {
		fallingBlock.setDropItem(false);
		fallingBlock.getPersistentDataContainer().set(ashKey, PersistentDataType.BYTE, (byte) 1);
	}

	public boolean isTagged(Entity entity) {
		return entity.getPersistentDataContainer().has(ashKey, PersistentDataType.BYTE);
	}

	public boolean isAsh(Block block) {
		final Set<Long> chunkPositions = positions(block.getChunk());
		final long packed = BlockKey.of(block).pack();

		if(!chunkPositions.contains(packed)) {
			return false;
		}
		if(!fire.getSettings().ash().isAshMaterial(block.getType())) {
			chunkPositions.remove(packed);
			persist(block.getChunk());
			return false;
		}
		return true;
	}

	public void mark(Block block) {
		if(positions(block.getChunk()).add(BlockKey.of(block).pack())) {
			persist(block.getChunk());
		}
		fresh.put(BlockKey.of(block), fire.getTick() + fire.getSettings().ash().particleDuration());
	}

	public void unmark(Block block) {
		if(positions(block.getChunk()).remove(BlockKey.of(block).pack())) {
			persist(block.getChunk());
		}
		fresh.remove(BlockKey.of(block));
	}

	public void unloadChunk(Chunk chunk) {
		final ChunkRef ref = ChunkRef.of(chunk);
		positions.remove(ref);
		fresh.keySet().removeIf(key -> key.chunk().equals(ref));
	}

	public int tickParticles(long tick, int budget) {
		int used = 0;
		final Iterator<Map.Entry<BlockKey, Long>> iterator = fresh.entrySet().iterator();

		while(iterator.hasNext()) {
			final Map.Entry<BlockKey, Long> entry = iterator.next();
			if(entry.getValue() <= tick) {
				iterator.remove();
				continue;
			}
			if(used >= budget) {
				continue;
			}

			final Block block = entry.getKey().toBlock();
			if(block == null) {
				iterator.remove();
				continue;
			}

			final Location location = block.getLocation().add(0.5, 1.1, 0.5);
			block.getWorld().spawnParticle(Particle.WHITE_ASH, location, 3, 0.4, 0.2, 0.4, 0);
			used += 3;
		}

		return used;
	}

	private Set<Long> positions(Chunk chunk) {
		return positions.computeIfAbsent(ChunkRef.of(chunk), ref -> {
			final long[] stored = chunk.getPersistentDataContainer().get(ashKey, PersistentDataType.LONG_ARRAY);
			final Set<Long> set = new HashSet<>();
			if(stored != null) {
				for(long packed : stored) {
					set.add(packed);
				}
			}
			return set;
		});
	}

	private void persist(Chunk chunk) {
		final PersistentDataContainer container = chunk.getPersistentDataContainer();
		final Set<Long> chunkPositions = positions(chunk);

		if(chunkPositions.isEmpty()) {
			container.remove(ashKey);
			return;
		}
		container.set(ashKey, PersistentDataType.LONG_ARRAY, chunkPositions.stream().mapToLong(Long::longValue).toArray());
	}
}

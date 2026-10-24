package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FireStorage {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Fire");

	private final NamespacedKey burningKey;

	public record StoredFire(BlockKey key, double remainingTicks, double heat, boolean smouldering) {}

	public FireStorage(Plugin plugin) {
		this.burningKey = new NamespacedKey(plugin, "burning");
	}

	public void save(Chunk chunk, Collection<BurningBlock> blocks) {
		final PersistentDataContainer container = chunk.getPersistentDataContainer();

		if(blocks.isEmpty()) {
			container.remove(burningKey);
			return;
		}

		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try(DataOutputStream out = new DataOutputStream(bytes)) {
			out.writeInt(blocks.size());
			for(BurningBlock burningBlock : blocks) {
				out.writeLong(burningBlock.getKey().pack());
				out.writeDouble(burningBlock.getRemainingTicks());
				out.writeDouble(burningBlock.getHeat());
				out.writeBoolean(!burningBlock.isBurning());
			}
		}
		catch(IOException e) {
			_log.error("[save] Unable to save burning blocks of chunk {} {}", chunk.getX(), chunk.getZ(), e);
			return;
		}

		container.set(burningKey, PersistentDataType.BYTE_ARRAY, bytes.toByteArray());
	}

	public List<StoredFire> load(Chunk chunk) {
		final PersistentDataContainer container = chunk.getPersistentDataContainer();
		final byte[] data = container.get(burningKey, PersistentDataType.BYTE_ARRAY);
		final List<StoredFire> fires = new ArrayList<>();

		if(data == null) {
			return fires;
		}
		container.remove(burningKey);

		try(DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
			final int size = in.readInt();
			for(int i = 0; i < size; i++) {
				fires.add(new StoredFire(BlockKey.unpack(chunk.getWorld().getUID(), in.readLong()), in.readDouble(), in.readDouble(), in.readBoolean()));
			}
		}
		catch(IOException e) {
			_log.error("[load] Unable to read burning blocks of chunk {} {}", chunk.getX(), chunk.getZ(), e);
		}

		return fires;
	}
}

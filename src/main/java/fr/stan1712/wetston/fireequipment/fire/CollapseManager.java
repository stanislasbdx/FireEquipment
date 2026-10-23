package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.FallingBlock;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class CollapseManager {
	private final Plugin plugin;
	private final FireManager fire;
	private final NamespacedKey burningKey;

	public CollapseManager(Plugin plugin, FireManager fire) {
		this.plugin = plugin;
		this.fire = fire;
		this.burningKey = new NamespacedKey(plugin, "burning");
	}

	public void collapseAbove(Block removed) {
		final FireSettings.Collapse collapse = fire.getSettings().collapse();
		if(!collapse.enabled()) {
			return;
		}

		final World world = removed.getWorld();
		for(int i = 1; i <= collapse.maxHeight(); i++) {
			final Block above = FireBlocks.blockAt(world, removed.getX(), removed.getY() + i, removed.getZ());
			if(above == null || !canFall(above, collapse) || isSupported(above, collapse)) {
				return;
			}
			drop(above);
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

	private boolean canFall(Block block, FireSettings.Collapse collapse) {
		final Material type = block.getType();
		final BlockData data = block.getBlockData();

		return type.isSolid()
				&& !type.hasGravity()
				&& !collapse.excluded().contains(type)
				&& !(data instanceof Bisected)
				&& !(data instanceof Bed)
				&& !(block.getState() instanceof TileState);
	}

	private boolean isSupported(Block block, FireSettings.Collapse collapse) {
		final Block below = FireBlocks.neighbor(block, BlockFace.DOWN);
		if(below == null || below.getType().isSolid()) {
			return true;
		}

		int sideSupport = 0;
		for(BlockFace face : FireBlocks.HORIZONTAL_FACES) {
			final Block side = FireBlocks.neighbor(block, face);
			if(side == null || (side.getType().isSolid() && !fire.isBurningAt(BlockKey.of(side)))) {
				sideSupport++;
			}
		}

		return sideSupport >= collapse.minSideSupport();
	}

	private void drop(Block block) {
		final BlockData data = block.getBlockData();
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

		block.getWorld().playSound(location, data.getSoundGroup().getBreakSound(), 1f, 0.6f);
	}
}

package fr.stan1712.wetston.fireequipment.tools;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;

final class Protection {
	private Protection() {
		throw new IllegalStateException("Utility class");
	}

	static boolean canPlace(Player player, Block block) {
		final BlockPlaceEvent event = new BlockPlaceEvent(block, block.getState(), block.getRelative(BlockFace.DOWN), player.getInventory().getItemInMainHand(), player, true, EquipmentSlot.HAND);
		Bukkit.getPluginManager().callEvent(event);

		return !event.isCancelled() && event.canBuild();
	}

	static boolean canBreak(Player player, Block block) {
		final BlockBreakEvent event = new BlockBreakEvent(block, player);
		Bukkit.getPluginManager().callEvent(event);

		return !event.isCancelled();
	}
}

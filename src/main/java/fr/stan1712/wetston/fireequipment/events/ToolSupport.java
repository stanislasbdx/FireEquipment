package fr.stan1712.wetston.fireequipment.events;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

final class ToolSupport {
	private ToolSupport() {
		throw new IllegalStateException("Utility class");
	}

	static boolean isMainHandRightClick(PlayerInteractEvent event) {
		final Action action = event.getAction();

		return event.getHand() == EquipmentSlot.HAND && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);
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

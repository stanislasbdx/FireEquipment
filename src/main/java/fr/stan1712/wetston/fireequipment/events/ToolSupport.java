package fr.stan1712.wetston.fireequipment.events;

import org.bukkit.event.block.Action;
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
}

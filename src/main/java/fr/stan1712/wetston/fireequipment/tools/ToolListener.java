package fr.stan1712.wetston.fireequipment.tools;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class ToolListener implements Listener {
	private final ItemFactory items;
	private final PluginSettings settings;
	private final Messages messages;
	private final Map<ToolType, FireTool> tools = new EnumMap<>(ToolType.class);
	private final Map<ToolType, Cooldowns> cooldowns = new EnumMap<>(ToolType.class);

	public ToolListener(ItemFactory items, PluginSettings settings, Messages messages, Collection<FireTool> registered) {
		this.items = items;
		this.settings = settings;
		this.messages = messages;

		registered.forEach(tool -> {
			tools.put(tool.type(), tool);
			cooldowns.put(tool.type(), new Cooldowns());
		});
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		final Action action = event.getAction();
		if(event.getHand() != EquipmentSlot.HAND || (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)) return;

		final Player player = event.getPlayer();
		final Optional<ToolType> type = items.identify(player.getInventory().getItemInMainHand());
		if(type.isEmpty() || !tools.containsKey(type.get())) return;

		event.setCancelled(true);

		if(!player.hasPermission(type.get().permission())) {
			messages.noPermission(player);
			return;
		}

		final long cooldownMillis = settings.tool(type.get()).cooldownMillis();
		if(!cooldowns.get(type.get()).tryUse(player.getUniqueId(), System.currentTimeMillis(), cooldownMillis)) return;

		tools.get(type.get()).use(player);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		cooldowns.values().forEach(cooldown -> cooldown.clear(event.getPlayer().getUniqueId()));
	}

	public void shutdown() {
		tools.values().forEach(FireTool::shutdown);
	}
}

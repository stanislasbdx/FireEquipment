package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import fr.stan1712.wetston.fireequipment.tools.ItemFactory;
import fr.stan1712.wetston.fireequipment.tools.ToolType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

class GiveSubCommand implements SubCommand {
	private final ToolType type;
	private final ItemFactory items;
	private final PluginSettings settings;
	private final Messages messages;

	GiveSubCommand(ToolType type, ItemFactory items, PluginSettings settings, Messages messages) {
		this.type = type;
		this.items = items;
		this.settings = settings;
		this.messages = messages;
	}

	@Override
	public String name() {
		return type.argument();
	}

	@Override
	public String permission() {
		return "firequip.tools.give";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			messages.send(sender, "[" + settings.prefix() + "] This command can only be used by a player.");
			return;
		}
		if(!player.hasPermission(type.permission())) {
			messages.noPermission(player);
			return;
		}

		player.getInventory().addItem(items.create(type)).values().forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
		messages.box(player, messages.get(type.giveMessagePath()));
	}

	static void sendItemsHelp(CommandSender sender, PluginSettings settings, Messages messages) {
		final List<String> lines = new ArrayList<>();
		lines.add(messages.get("Core.GiveMsg.Home"));

		for(ToolType type : ToolType.values()) {
			lines.add(type.argument() + " = " + settings.tool(type).displayName());
		}

		messages.box(sender, lines);
	}
}

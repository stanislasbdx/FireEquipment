package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import fr.stan1712.wetston.fireequipment.tools.ItemFactory;
import fr.stan1712.wetston.fireequipment.tools.ToolType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GiveItem implements CommandExecutor {
	private final ItemFactory items;
	private final PluginSettings settings;
	private final Messages messages;

	public GiveItem(ItemFactory items, PluginSettings settings, Messages messages) {
		this.items = items;
		this.settings = settings;
		this.messages = messages;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player player)) {
			messages.send(sender, "[" + settings.prefix() + "] This command can only be used by a player.");
			return true;
		}

		if(!player.hasPermission("firequip.tools.give")) {
			messages.noPermission(player);
			return true;
		}

		final Optional<ToolType> type = args.length == 1 ? ToolType.fromArgument(args[0]) : Optional.empty();
		if(type.isEmpty()) {
			sendItemsHelp(player);
			return true;
		}

		player.getInventory().addItem(items.create(type.get()));
		messages.box(player, messages.get(type.get().giveMessagePath()));

		return true;
	}

	private void sendItemsHelp(Player player) {
		final List<String> lines = new ArrayList<>();
		lines.add(messages.get("Core.GiveMsg.Home"));

		for(ToolType type : ToolType.values()) {
			lines.add(type.argument() + " = " + settings.tool(type).displayName());
		}

		messages.box(player, lines);
	}
}

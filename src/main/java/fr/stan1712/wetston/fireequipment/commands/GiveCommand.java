package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import fr.stan1712.wetston.fireequipment.tools.ItemFactory;
import fr.stan1712.wetston.fireequipment.tools.ToolType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class GiveCommand implements TabExecutor {
	private static final String GIVE_PERMISSION = "firequip.tools.give";

	private final ItemFactory items;
	private final PluginSettings settings;
	private final Messages messages;

	public GiveCommand(ItemFactory items, PluginSettings settings, Messages messages) {
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

		if(!player.hasPermission(GIVE_PERMISSION)) {
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

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length != 1 || !sender.hasPermission(GIVE_PERMISSION)) return List.of();

		final String typed = args[0].toLowerCase(Locale.ROOT);

		return Arrays.stream(ToolType.values())
			.filter(type -> sender.hasPermission(type.permission()))
			.map(ToolType::argument)
			.filter(argument -> argument.startsWith(typed))
			.toList();
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

package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.fire.FireInspector;
import fr.stan1712.wetston.fireequipment.fire.FireManager;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import fr.stan1712.wetston.fireequipment.tools.ItemFactory;
import fr.stan1712.wetston.fireequipment.tools.ToolType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FireEquipmentCommand implements TabExecutor {
	private static final String BASE_PERMISSION = "firequip.info";
	private static final String GIVE_PERMISSION = "firequip.tools.give";

	private final PluginSettings settings;
	private final Messages messages;
	private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

	public FireEquipmentCommand(Plugin plugin, PluginSettings settings, Messages messages, FireManager fire, ItemFactory items) {
		this.settings = settings;
		this.messages = messages;

		register(new HelpSubCommand(messages));
		register(new VersionSubCommand(plugin, messages));
		register(new ReloadSubCommand(settings, messages, fire));
		register(new FireSubCommand(fire, messages, new FireInspector(plugin, fire, messages)));
		for(ToolType type : ToolType.values()) {
			register(new GiveSubCommand(type, items, settings, messages));
		}
	}

	private void register(SubCommand subCommand) {
		subCommands.put(subCommand.name(), subCommand);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		final SubCommand subCommand = args.length >= 1 ? subCommands.get(args[0].toLowerCase(Locale.ROOT)) : null;
		if(subCommand != null) {
			if(sender.hasPermission(subCommand.permission())) {
				subCommand.execute(sender, args);
			} else {
				messages.noPermission(sender);
			}
		} else if(sender.hasPermission(BASE_PERMISSION)) {
			sendOverview(sender);
		} else if(sender.hasPermission(GIVE_PERMISSION)) {
			GiveSubCommand.sendItemsHelp(sender, settings, messages);
		} else {
			messages.noPermission(sender);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 1) {
			final String typed = args[0].toLowerCase(Locale.ROOT);

			return subCommands.values().stream()
				.filter(subCommand -> sender.hasPermission(subCommand.permission()))
				.map(SubCommand::name)
				.filter(name -> name.startsWith(typed))
				.toList();
		}

		final SubCommand subCommand = args.length > 1 ? subCommands.get(args[0].toLowerCase(Locale.ROOT)) : null;
		if(subCommand == null || !sender.hasPermission(subCommand.permission())) return List.of();

		return subCommand.complete(sender, args);
	}

	private void sendOverview(CommandSender sender) {
		messages.box(sender,
			messages.get("Core.HelpMsg.Help"),
			messages.get("Core.HelpMsg.VersionHelp"),
			messages.get("Core.HelpMsg.ReloadHelp")
		);
	}
}

package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.config.ConfigMigrator;
import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class FireEquipment implements CommandExecutor {
	private final Plugin plugin;
	private final PluginSettings settings;
	private final Messages messages;

	public FireEquipment(Plugin plugin, PluginSettings settings, Messages messages) {
		this.plugin = plugin;
		this.settings = settings;
		this.messages = messages;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("firequip.info")) {
			messages.noPermission(sender);
			return true;
		}

		final String sub = args.length >= 1 ? args[0].toLowerCase() : "";

		switch (sub) {
			case "version" -> messages.box(sender, "Version " + plugin.getConfig().getString(ConfigMigrator.VERSION_KEY));
			case "help" -> messages.box(sender,
				"/firequip help = " + messages.get("Core.HelpMsg.DHelp"),
				"/firequip version = " + messages.get("Core.HelpMsg.DVersion"),
				"/firequip reload = " + messages.get("Core.HelpMsg.DReload"),
				"/fequip <item> = " + messages.get("Core.HelpMsg.DGive")
			);
			case "reload" -> {
				if(sender.hasPermission("firequip.admin.reload")) {
					settings.reload();
					messages.box(sender, messages.get("Core.Reload"));
				} else {
					messages.noPermission(sender);
				}
			}
			default -> messages.box(sender,
				messages.get("Core.HelpMsg.Help"),
				messages.get("Core.HelpMsg.VersionHelp"),
				messages.get("Core.HelpMsg.ReloadHelp")
			);
		}

		return true;
	}
}

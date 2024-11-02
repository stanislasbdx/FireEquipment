package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.Config;
import fr.stan1712.wetston.fireequipment.Main;
import fr.stan1712.wetston.fireequipment.defaults.StrStructure;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import static fr.stan1712.wetston.fireequipment.Utils.ConfigFactory.getConfigString;

public class FireEquipment implements CommandExecutor {
	private final Plugin pl;

	public FireEquipment(Main pl) {
		this.pl = pl;
	}

	public static final String PREFIX_LITT = "Prefix";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("firequip.info")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("version")) {
					sender.sendMessage(StrStructure.START_TITLE_BOX + getConfigString(PREFIX_LITT) + StrStructure.END_TITLE_BOX);
					sender.sendMessage(ChatColor.WHITE + "» Version " + getConfigString("Version"));
					sender.sendMessage(StrStructure.BOTTOM_BOX);
				} else if (args[0].equalsIgnoreCase("help")) {
					sender.sendMessage(StrStructure.START_TITLE_BOX + getConfigString(PREFIX_LITT) + StrStructure.END_TITLE_BOX);
					sender.sendMessage(ChatColor.WHITE + "» /firequip help = " + getConfigString("Core.HelpMsg.DHelp"));
					sender.sendMessage(ChatColor.WHITE + "» /firequip version = " + getConfigString("Core.HelpMsg.DVersion"));
					sender.sendMessage(ChatColor.WHITE + "» /firequip reload = " + getConfigString("Core.HelpMsg.DReload"));
					sender.sendMessage(ChatColor.WHITE + "» /equip <item> = " + getConfigString("Core.HelpMsg.DGive"));
					sender.sendMessage(StrStructure.BOTTOM_BOX);
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("firequip.admin.reload")) {
						new Config();
						this.pl.saveConfig();

						sender.sendMessage(StrStructure.START_TITLE_BOX + getConfigString(PREFIX_LITT) + StrStructure.END_TITLE_BOX);
						sender.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.Reload"));
						sender.sendMessage(StrStructure.BOTTOM_BOX);
					} else {
						sender.sendMessage("[" + getConfigString(PREFIX_LITT) + "]" + getConfigString("Core.NoPerms"));
					}
				}
			} else {
				sender.sendMessage(StrStructure.START_TITLE_BOX + getConfigString(PREFIX_LITT) + StrStructure.END_TITLE_BOX);
				sender.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.HelpMsg.Help"));
				sender.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.HelpMsg.VersionHelp"));
				sender.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.HelpMsg.ReloadHelp"));
				sender.sendMessage(StrStructure.BOTTOM_BOX);
			}
		} else {
			sender.sendMessage("[" + getConfigString(PREFIX_LITT) + "]" + getConfigString("Core.NoPerms"));
		}

		return true;
	}
}

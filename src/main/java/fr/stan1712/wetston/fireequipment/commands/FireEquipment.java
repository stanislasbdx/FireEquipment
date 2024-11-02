package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.Config;
import fr.stan1712.wetston.fireequipment.Main;
import fr.stan1712.wetston.fireequipment.defaults.StrStructure;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class FireEquipment implements CommandExecutor {
	private final Plugin pl;

	public FireEquipment(Main pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("firequip.info")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("version")) {
					sender.sendMessage(StrStructure.START_TITLE_BOX + this.pl.getConfig().getString("Prefix").replace("&", "§") + StrStructure.END_TITLE_BOX);
					sender.sendMessage(ChatColor.WHITE + "» Version " + this.pl.getConfig().getString("Version").replace("&", "§"));
					sender.sendMessage(StrStructure.BOTTOM_BOX);
				} else if (args[0].equalsIgnoreCase("help")) {
					sender.sendMessage(StrStructure.START_TITLE_BOX + this.pl.getConfig().getString("Prefix").replace("&", "§") + StrStructure.END_TITLE_BOX);
					sender.sendMessage(ChatColor.WHITE + "» /firequip help = " + this.pl.getConfig().getString("Core.HelpMsg.DHelp").replace("&", "§"));
					sender.sendMessage(ChatColor.WHITE + "» /firequip version = " + this.pl.getConfig().getString("Core.HelpMsg.DVersion").replace("&", "§"));
					sender.sendMessage(ChatColor.WHITE + "» /firequip reload = " + this.pl.getConfig().getString("Core.HelpMsg.DReload").replace("&", "§"));
					sender.sendMessage(ChatColor.WHITE + "» /equip <item> = " + this.pl.getConfig().getString("Core.HelpMsg.DGive").replace("&", "§"));
					sender.sendMessage(StrStructure.BOTTOM_BOX);
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("firequip.admin.reload")) {
						new Config();
						this.pl.saveConfig();

						sender.sendMessage(StrStructure.START_TITLE_BOX + this.pl.getConfig().getString("Prefix").replace("&", "§") + StrStructure.END_TITLE_BOX);
						sender.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.Reload").replace("&", "§"));
						sender.sendMessage(StrStructure.BOTTOM_BOX);
					} else {
						sender.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
					}
				}
			} else {
				sender.sendMessage(StrStructure.START_TITLE_BOX + this.pl.getConfig().getString("Prefix").replace("&", "§") + StrStructure.END_TITLE_BOX);
				sender.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.HelpMsg.Help").replace("&", "§"));
				sender.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.HelpMsg.VersionHelp").replace("&", "§"));
				sender.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.HelpMsg.ReloadHelp").replace("&", "§"));
				sender.sendMessage(StrStructure.BOTTOM_BOX);
			}
		} else {
			sender.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
		}

		return true;
	}
}

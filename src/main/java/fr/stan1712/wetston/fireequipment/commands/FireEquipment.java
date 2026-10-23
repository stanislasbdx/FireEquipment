package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.utils.Config;
import fr.stan1712.wetston.fireequipment.Main;
import fr.stan1712.wetston.fireequipment.defaults.StrStructure;
import fr.stan1712.wetston.fireequipment.fire.BlockKey;
import fr.stan1712.wetston.fireequipment.fire.FireManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static fr.stan1712.wetston.fireequipment.utils.Utils.ConfigFactory.getConfigString;

public class FireEquipment implements CommandExecutor {
	private final Main pl;

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
					sender.sendMessage(ChatColor.WHITE + "» /firequip fire <stats/clear [radius]> = " + getConfigString("Core.HelpMsg.DFire"));
					sender.sendMessage(ChatColor.WHITE + "» /equip <item> = " + getConfigString("Core.HelpMsg.DGive"));
					sender.sendMessage(StrStructure.BOTTOM_BOX);
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("firequip.admin.reload")) {
						new Config();
						this.pl.saveConfig();
						this.pl.getFireManager().reload();

						sender.sendMessage(StrStructure.START_TITLE_BOX + getConfigString(PREFIX_LITT) + StrStructure.END_TITLE_BOX);
						sender.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.Reload"));
						sender.sendMessage(StrStructure.BOTTOM_BOX);
					} else {
						sender.sendMessage("[" + getConfigString(PREFIX_LITT) + "]" + getConfigString("Core.NoPerms"));
					}
				} else if (args[0].equalsIgnoreCase("fire")) {
					if (sender.hasPermission("firequip.admin.fire")) {
						fireCommand(sender, args);
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

	private void fireCommand(CommandSender sender, String[] args) {
		final FireManager fireManager = this.pl.getFireManager();

		sender.sendMessage(StrStructure.START_TITLE_BOX + getConfigString(PREFIX_LITT) + StrStructure.END_TITLE_BOX);

		if (args.length >= 2 && args[1].equalsIgnoreCase("clear")) {
			int cleared;

			if (args.length >= 3 && sender instanceof Player player) {
				try {
					cleared = fireManager.clear(BlockKey.of(player.getLocation().getBlock()), Integer.parseInt(args[2]));
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.WHITE + "» /firequip fire clear [radius]");
					sender.sendMessage(StrStructure.BOTTOM_BOX);
					return;
				}
			} else {
				cleared = fireManager.clear(null, 0);
			}

			sender.sendMessage(ChatColor.WHITE + "» " + cleared + " " + getConfigString("Core.FireMsg.Cleared"));
		} else {
			final long smouldering = fireManager.all().stream().filter(burningBlock -> !burningBlock.isBurning()).count();
			final boolean enabled = fireManager.getSettings().enabled();

			sender.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.FireMsg.Status") + " : " + (enabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
			sender.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.FireMsg.Worlds") + " : " + String.join(", ", fireManager.getSettings().worlds()));
			sender.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.FireMsg.Burning") + " : " + (fireManager.all().size() - smouldering));
			sender.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.FireMsg.Smouldering") + " : " + smouldering);
		}

		sender.sendMessage(StrStructure.BOTTOM_BOX);
	}
}

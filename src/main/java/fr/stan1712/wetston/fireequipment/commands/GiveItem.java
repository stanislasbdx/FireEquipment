package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.Main;
import fr.stan1712.wetston.fireequipment.defaults.Items;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import static fr.stan1712.wetston.fireequipment.Utils.ConfigFactory.getConfigString;

public class GiveItem implements CommandExecutor {
	private final Plugin pl;

	public GiveItem(Main pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) return false;

		if(player.hasPermission("firequip.tools.give")) {
			Items items = new Items(this.pl);

			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("hose")) {
					giveItemToPlayer(
						items.getHoseItem(),
						player,
						"Hose"
					);
				}
				else if(args[0].equalsIgnoreCase("pump")) {
					giveItemToPlayer(
						items.getPumpItem(),
						player,
						"Pump"
					);
				}
				else if(args[0].equalsIgnoreCase("extinguisher")) {
					giveItemToPlayer(
						items.getExtinguisherItem(),
						player,
						"Extinguisher"
					);
				}
				else {
					sendItemsHelp(player);
				}
			}
			else {
				sendItemsHelp(player);
			}
		}

		return true;
	}

	private void sendItemsHelp(Player player) {
		player.sendMessage(ChatColor.RED + "+----- ▲ " + getConfigString("Prefix") + " ▲ -----+");
		player.sendMessage(ChatColor.WHITE + "   " + getConfigString("Core.GiveMsg.Home"));
		player.sendMessage(ChatColor.WHITE + "» hose = " + getConfigString("Equipment.Hose.displayName"));
		player.sendMessage(ChatColor.WHITE + "» pump = " + getConfigString("Equipment.Pump.displayName"));
		player.sendMessage(ChatColor.WHITE + "» extinguisher = " + getConfigString("Equipment.Extinguisher.displayName"));
		player.sendMessage(ChatColor.RED + "+----- ----- ----- -----+");
	}

	private void giveItemToPlayer(ItemStack item, Player player, String path) {
		player.getInventory().addItem(item);
		player.sendMessage(ChatColor.WHITE + "» " + getConfigString("Core.GiveMsg." + path));
	}
}

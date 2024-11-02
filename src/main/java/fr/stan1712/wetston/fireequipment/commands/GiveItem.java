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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GiveItem implements CommandExecutor {
	private final Plugin pl;
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - GiveItem");

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
		player.sendMessage(ChatColor.RED + "+----- ▲ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ▲ -----+");
		player.sendMessage(ChatColor.WHITE + "   " + this.pl.getConfig().getString("Core.GiveMsg.Home").replace("&", "§"));
		player.sendMessage(ChatColor.WHITE + "» hose = " + this.pl.getConfig().getString("Equipment.Hose.displayName").replace("&", "§"));
		player.sendMessage(ChatColor.WHITE + "» pump = " + this.pl.getConfig().getString("Equipment.Pump.displayName").replace("&", "§"));
		player.sendMessage(ChatColor.WHITE + "» extinguisher = " + this.pl.getConfig().getString("Equipment.Extinguisher.displayName").replace("&", "§"));
		player.sendMessage(ChatColor.RED + "+----- ----- ----- -----+");
	}

	private void giveItemToPlayer(ItemStack item, Player player, String path) {
		player.getInventory().addItem(item);
		player.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.GiveMsg." + path).replace("&", "§"));
	}
}

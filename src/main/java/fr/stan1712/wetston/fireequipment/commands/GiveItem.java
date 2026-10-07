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

import static fr.stan1712.wetston.fireequipment.utils.Utils.ConfigFactory.getConfigString;

public class GiveItem implements CommandExecutor {
	private final Plugin pl;

	public GiveItem(Main pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("[" + getConfigString("Prefix") + "] This command can only be used by a player.");
			return true;
		}

		if(!player.hasPermission("firequip.tools.give")) {
			player.sendMessage("[" + getConfigString("Prefix") + "]" + getConfigString("Core.NoPerms"));
			return true;
		}

		final Items items = new Items(this.pl);

		if(args.length != 1) {
			sendItemsHelp(player);
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "hose" -> giveItemToPlayer(items.getHoseItem(), player, "Hose");
			case "pump" -> giveItemToPlayer(items.getPumpItem(), player, "Pump");
			case "extinguisher" -> giveItemToPlayer(items.getExtinguisherItem(), player, "Extinguisher");
			default -> sendItemsHelp(player);
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

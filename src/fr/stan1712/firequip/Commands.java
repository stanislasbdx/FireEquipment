package fr.stan1712.firequip;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Commands implements Listener{
    private Main pl;
  
    public Commands(Main pl) {
        this.pl = pl;
        pl.getConfig();
    }

	@EventHandler
	public void onCommandes(PlayerCommandPreprocessEvent e){
        Player player = e.getPlayer();
        String msg = e.getMessage();
        String[] args = msg.split(" ");
        
		// /firequip <version/help>
		if(args[0].equalsIgnoreCase("/firequip")) {
			if(player.hasPermission("firequip.info")) {
				if(args.length == 2) {
					if(args[1].equalsIgnoreCase("version")) {
			            player.sendMessage(ChatColor.RED + "+----- ▲ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ▲ -----+");
			            player.sendMessage(ChatColor.WHITE + "» Version " + this.pl.getConfig().getString("Version").replace("&", "§"));
			            player.sendMessage(ChatColor.RED + "+----- ----- ----- -----+");
					}
					else if(args[1].equalsIgnoreCase("help")) {
			            player.sendMessage(ChatColor.RED + "+----- ----- ----- -----+");
			            player.sendMessage(ChatColor.WHITE + "» /firequip help = " + this.pl.getConfig().getString("Core.HelpMsg.DVersion").replace("&", "§"));
			            player.sendMessage(ChatColor.WHITE + "» /firequip version = " + this.pl.getConfig().getString("Core.HelpMsg.DHelp").replace("&", "§"));
			            player.sendMessage(ChatColor.WHITE + "» /equip <item> = " + this.pl.getConfig().getString("Core.HelpMsg.DGive").replace("&", "§"));
			            player.sendMessage(ChatColor.RED + "+----- ----- ----- -----+");
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "+----- ▲ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ▲ -----+");
					player.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.HelpMsg.Help").replace("&", "§"));
					player.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.HelpMsg.VersionHelp").replace("&", "§"));
					player.sendMessage(ChatColor.RED + "+----- ----- ----- -----+");
				}
			}
			else{
				player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
			}
		}
		
		if(args[0].equalsIgnoreCase("/equip")) {
			if(player.hasPermission("firequip.tools.give")) {
				if(args.length == 2) {
					if(args[1].equalsIgnoreCase("hose")) {
				        ItemStack hose = new ItemStack(Material.GOLDEN_HOE);
				        ItemMeta meta = hose.getItemMeta();
				       
				        meta.addEnchant(Enchantment.PROTECTION_FIRE, 10, true);
				        meta.setDisplayName(ChatColor.RED + this.pl.getConfig().getString("Equipment.Hose.displayName").replace("&", "§"));
				        hose.setItemMeta(meta);
				        
				        player.getInventory().addItem(hose);
						player.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.GiveMsg.Hose").replace("&", "§"));
					}
					if(args[1].equalsIgnoreCase("pump")) {
				        ItemStack pump = new ItemStack(Material.CLAY_BALL);
				        ItemMeta meta = pump.getItemMeta();
				       
				        meta.addEnchant(Enchantment.WATER_WORKER, 10, true);
				        meta.setDisplayName(ChatColor.BLUE + this.pl.getConfig().getString("Equipment.Pump.displayName").replace("&", "§"));
				        pump.setItemMeta(meta);
				        
				        player.getInventory().addItem(pump);
						player.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.GiveMsg.Pump").replace("&", "§"));
					}
					if(args[1].equalsIgnoreCase("extinguisher")) {
				        ItemStack extinguisher = new ItemStack(Material.IRON_HOE);
				        ItemMeta meta = extinguisher.getItemMeta();

				        meta.addEnchant(Enchantment.PROTECTION_FIRE, 10, true);
				        meta.setDisplayName(ChatColor.WHITE + this.pl.getConfig().getString("Equipment.Extinguisher.displayName").replace("&", "§"));
				        extinguisher.setItemMeta(meta);
				        
				        player.getInventory().addItem(extinguisher);
						player.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Core.GiveMsg.Extinguisher").replace("&", "§"));
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "+----- ▲ " + this.pl.getConfig().getString("Prefix").replace("&", "§") + " ▲ -----+");
					player.sendMessage(ChatColor.WHITE + "   " + this.pl.getConfig().getString("Core.GiveMsg.Home").replace("&", "§"));
					player.sendMessage(ChatColor.WHITE + "» hose = " + this.pl.getConfig().getString("Equipment.Hose.displayName").replace("&", "§"));
					player.sendMessage(ChatColor.WHITE + "» pump = " + this.pl.getConfig().getString("Equipment.Pump.displayName").replace("&", "§"));
					player.sendMessage(ChatColor.WHITE + "» extinguisher = " + this.pl.getConfig().getString("Equipment.Extinguisher.displayName").replace("&", "§"));
					player.sendMessage(ChatColor.RED + "+----- ----- ----- -----+");
				}
			}
		}
	}
}

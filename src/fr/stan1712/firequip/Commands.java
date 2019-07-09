package fr.stan1712.firequip;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

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
			            player.sendMessage(ChatColor.WHITE + "» " + this.pl.getConfig().getString("Version").replace("&", "§"));
			            player.sendMessage(ChatColor.RED + "+----- ----- ----- -----+");
					}
					else if(args[1].equalsIgnoreCase("help")) {
			            //player.sendMessage(ChatColor.GOLD + "» /mobile = " + this.pl.getConfig().getString("Core.HelpMsg.DPhone").replace("&", "§"));
			            player.sendMessage(ChatColor.RED + "+----- ----- ----- -----+");
			            player.sendMessage(ChatColor.WHITE + "» /firequip help = " + this.pl.getConfig().getString("Core.HelpMsg.DVersion").replace("&", "§"));
			            player.sendMessage(ChatColor.WHITE + "» /firequip version = " + this.pl.getConfig().getString("Core.HelpMsg.DHelp").replace("&", "§"));
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
	}
}

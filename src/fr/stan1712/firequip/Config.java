package fr.stan1712.firequip;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class Config implements Listener {
	private Plugin plugin = Main.getPlugin(Main.class);
	
	String version = "Version 1.5";

	private void VersionUpdate() {
		FileConfiguration config = plugin.getConfig();
		plugin.getConfig();
		    
		config.set("Version", version);
	}
	
	public Config() {
	    FileConfiguration config = plugin.getConfig();
		plugin.getConfig();
	    
	    VersionUpdate();
	    
	    config.options().header("FireEquipment | Owner : stan1712 \nTraductors : ErHak_ / legaming04 -> https://github.com/stan1712/FireEquipement/wiki/Translations \nOur Discord : https://discord.gg/DkQSQa7");
	    if(this.plugin.getConfig().getBoolean("ConfigFix")) {
	      config.options().copyDefaults(true);
	      config.options().copyHeader(true);
	      
	      config.set("ConfigFix", Boolean.valueOf(false));
	      
	      plugin.getServer().getConsoleSender().sendMessage("[FireEquipment] " + ChatColor.GREEN + "config.yml fixed !");
	    }
	    
		plugin.getServer().getConsoleSender().sendMessage("[FireEquipment] " + ChatColor.GREEN + "Config file up and running !");
	}
}

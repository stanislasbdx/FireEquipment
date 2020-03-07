package fr.stan1712.firequip;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class Config implements Listener {
	private Plugin plugin = Main.getPlugin(Main.class);

	String version = this.plugin.getDescription().getVersion();
	String fileVersion = this.plugin.getConfig().getString("Version");
	
	Boolean updatable;

	private void VersionUpdate() {
		plugin.getConfig();
		    
		if(!version.equals(fileVersion)) {
			plugin.getConfig().set("Version", version);
			plugin.getServer().getConsoleSender().sendMessage("[FireEquipment] " + ChatColor.GREEN + "config.yml upgraded (" + fileVersion + " -> " + version + ") !");
			
			plugin.getConfig().set("ConfigFix", Boolean.valueOf(true));
			new Config();
            plugin.saveConfig();
		}
	}
	
	public Config() {
	    FileConfiguration config = plugin.getConfig();
		plugin.getConfig();
	    
	    VersionUpdate();
	    config.options().header("FireEquipment | Owner : stan1712 \n"
	    		+ "Our Discord : https://discord.gg/DkQSQa7");
	    
	    if(config.getBoolean("ConfigFix")) {
	      config.options().copyDefaults(true);
	      config.options().copyHeader(true);
	      
	      config.set("ConfigFix", Boolean.valueOf(false));
	      
	      plugin.getServer().getConsoleSender().sendMessage("[FireEquipment] " + ChatColor.GREEN + "config.yml updated !");
	    }
	    
		plugin.getServer().getConsoleSender().sendMessage("[FireEquipment] " + ChatColor.GREEN + "Config file up and running !");
	}
}

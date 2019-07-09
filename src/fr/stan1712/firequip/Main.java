package fr.stan1712.firequip;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {	
	public void versionCheck() {
		String version = getServer().getVersion();
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + "Server version : " + version);
		if(version.contains("Spigot")) {
			if(version.contains("1.12")) {
				console.sendMessage("[FireEquipment] " + ChatColor.GREEN + "Version check !");
				console.sendMessage("[FireEquipment] " + ChatColor.GREEN + "If you got issues, you can report them on Github");
			}
			else if(version.contains("1.11")){
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "* 1.11 is not supported by FireEquipment *");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "* If you got errors, report it on Github *");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "*** *** ***");
			}
			else if(version.contains("1.10")){
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "* 1.10 is not supported by FireEquipment *");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "* If you got errors, don't report it on Github *");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "*** *** ***");
			}
			else if(version.contains("1.9")){
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "* 1.9 is not supported by FireEquipment *");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "* If you got errors, don't report it on Github *");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "*** *** ***");
			}
			else if(version.contains("1.8")){
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "*** WARNING ***");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "* 1.8 is not supported by FireEquipment *");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "* If you got errors, don't report it on Github *");
				console.sendMessage("[FireEquipment] " + ChatColor.RED + "*** *** ***");
			}
		}
		else {
			console.sendMessage("[FireEquipment] " + ChatColor.RED + "***  ***  UNKNOWN VERSION  ***  ***");
			console.sendMessage("[FireEquipment] " + ChatColor.RED + "* The plugin will be disabled now *");
			console.sendMessage("[FireEquipment] " + ChatColor.RED + "*** *** *** *** *** *** *** *** ***");
			pm.disablePlugins();
		}
	}
	
	PluginManager pm = getServer().getPluginManager();
	ConsoleCommandSender console = getServer().getConsoleSender();
	
	public void onEnable() {
		console.sendMessage("[FireEquipment] " + ChatColor.DARK_BLUE + "# ------ FireEquipment ------ #");
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + " ");
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + "FireEquipment " + getConfig().getString("Version"));
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + "Boot sequence launched !");
		
		/*
		 * Version checker
		 */
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + " ");
		console.sendMessage("[FireEquipment] " + ChatColor.DARK_BLUE + "#- Version check -#");
		versionCheck();
		
		/*
		 * Class loader (project)
		 */
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + " ");
		console.sendMessage("[FireEquipment] " + ChatColor.DARK_BLUE + "#- Class manager, loading classes -#");
		pm.registerEvents(new Events(this), this);
		console.sendMessage("[FireEquipment] " + ChatColor.GREEN + "All classes have been loaded");
		
		/*
		 * Commands loader
		 */
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + " ");
		console.sendMessage("[FireEquipment] " + ChatColor.DARK_BLUE + "#- Commands manager, loading commands -#");
		pm.registerEvents(new Commands(this), this);
		console.sendMessage("[FireEquipment] " + ChatColor.GREEN + "All commands have been loaded");
	      	
		
		/*
		 * Config creator/modificator
		 */
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + " ");
		console.sendMessage("[FireEquipment] " + ChatColor.DARK_BLUE + "#- Config writer, writing and creating configs files -#");
		pm.registerEvents(new Config(), this);
		saveConfig();

		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + " ");
		console.sendMessage("[FireEquipment] " + ChatColor.DARK_BLUE + "# ------ FireEquipment ------ #");
	}
	
	public void onDisable() {
		console.sendMessage("[FireEquipment] " + ChatColor.DARK_BLUE + "# ------ FireEquipment ------ #");
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + "FireEquipment " + getConfig().getString("Version"));
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + "Shutdown sequence launched !");
		
		/* 
		 * Disable statement
		 */
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + " ");
		console.sendMessage("[FireEquipment] " + ChatColor.DARK_BLUE + "#- Goodbye ! -#");
		
		console.sendMessage("[FireEquipment] " + ChatColor.BLUE + " ");
		console.sendMessage("[FireEquipment] " + ChatColor.DARK_BLUE + "# ------ FireEquipment ------ #");
	}

}

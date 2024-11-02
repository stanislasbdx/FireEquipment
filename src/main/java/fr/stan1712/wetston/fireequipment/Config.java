package fr.stan1712.wetston.fireequipment;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Config implements Listener {
	private final Plugin plugin = Main.getPlugin(Main.class);
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Config");

	String version = this.plugin.getDescription().getVersion();
	String fileVersion = this.plugin.getConfig().getString("Version");

	private void versionUpdate() {
		plugin.getConfig();

		if(!version.equals(fileVersion)) {
			plugin.getConfig().set("Version", version);
			plugin.getServer().getConsoleSender().sendMessage("Config file 'config.yml' upgraded (" + fileVersion + " -> " + version + ") !");

			plugin.getConfig().set("ConfigFix", Boolean.TRUE);
			new Config();
			plugin.saveConfig();
		}
	}

	public Config() {
		FileConfiguration config = plugin.getConfig();
		plugin.getConfig();

		versionUpdate();

		final ArrayList<String> headerStrings = new ArrayList<>();
		headerStrings.add("FireEquipment | Owner : stan1712");
		headerStrings.add("Our Discord : https://discord.gg/DkQSQa7");
		config.options().setHeader(headerStrings);

		if(config.getBoolean("ConfigFix")) {
			config.options().copyDefaults(true);
			config.options().parseComments(true);

			config.set("ConfigFix", Boolean.FALSE);

			_log.debug("Config file 'config.yml' updated !");
		}

		_log.info("Config file reloaded !");
	}
}

package fr.stan1712.wetston.fireequipment.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public final class ConfigMigrator {
	public static final String VERSION_KEY = "Version";
	private static final String LEGACY_FIX_KEY = "ConfigFix";
	private static final List<String> HEADER = List.of(
		"FireEquipment | Owner : stan1712",
		"Our Discord : https://discord.gg/DkQSQa7"
	);

	private ConfigMigrator() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean migrate(Plugin plugin) {
		plugin.saveDefaultConfig();

		final FileConfiguration config = plugin.getConfig();
		final String current = plugin.getDescription().getVersion();
		final String stored = config.getString(VERSION_KEY);

		if(current.equals(stored)) return false;

		config.options().copyDefaults(true);
		config.options().setHeader(HEADER);
		config.set(LEGACY_FIX_KEY, null);
		config.set(VERSION_KEY, current);
		plugin.saveConfig();

		plugin.getLogger().info("Config file 'config.yml' upgraded (" + stored + " -> " + current + ")");
		return true;
	}
}

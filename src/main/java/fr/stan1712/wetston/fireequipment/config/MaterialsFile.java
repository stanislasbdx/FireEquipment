package fr.stan1712.wetston.fireequipment.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class MaterialsFile {
	public static final String NAME = "materials.yml";

	private MaterialsFile() {
		throw new IllegalStateException("Utility class");
	}

	public static FileConfiguration load(Plugin plugin) {
		final File file = new File(plugin.getDataFolder(), NAME);
		if(!file.exists() && plugin.getResource(NAME) != null) {
			plugin.saveResource(NAME, false);
		}

		final YamlConfiguration materials = YamlConfiguration.loadConfiguration(file);
		final InputStream defaults = plugin.getResource(NAME);
		if(defaults != null) {
			materials.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaults, StandardCharsets.UTF_8)));
		}

		return materials;
	}
}

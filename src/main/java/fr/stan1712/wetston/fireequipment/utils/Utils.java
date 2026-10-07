package fr.stan1712.wetston.fireequipment.utils;

import fr.stan1712.wetston.fireequipment.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public class Utils {
	private Utils() {
		throw new IllegalStateException("Utility class");
	}

	private static FileConfiguration config() {
		return Main.getPlugin(Main.class).getConfig();
	}

	public static class ConfigFactory {
		private ConfigFactory() {
			throw new IllegalStateException("Utility class");
		}

		public static String getConfigString(String path) {
			return Objects.requireNonNull(config().getString(path)).replace("&", "§");
		}
		public static Boolean getConfigBoolean(String path) {
			return config().getBoolean(path);
		}
	}
}

package fr.stan1712.wetston.fireequipment.config;

import fr.stan1712.wetston.fireequipment.tools.ToolType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;

public final class PluginSettings {
	public static final String DEFAULT_PREFIX = "FireEquipment";
	public static final int DEFAULT_COOLDOWN_TICKS = 10;
	public static final int DEFAULT_WATER_LIFETIME_TICKS = 60;

	public record ToolSettings(String displayName, String usage, int range, int cooldownTicks) {
		public long cooldownMillis() {
			return cooldownTicks * 50L;
		}
	}

	public record Values(String prefix, Map<ToolType, ToolSettings> tools, int hoseWaterLifetimeTicks) {
	}

	private final Plugin plugin;
	private volatile Values values;

	public PluginSettings(Plugin plugin) {
		this.plugin = plugin;
		this.values = read(plugin.getConfig());
	}

	public void reload() {
		plugin.reloadConfig();
		values = read(plugin.getConfig());
	}

	public String prefix() {
		return values.prefix();
	}

	public ToolSettings tool(ToolType type) {
		return values.tools().get(type);
	}

	public int hoseWaterLifetimeTicks() {
		return values.hoseWaterLifetimeTicks();
	}

	public static Values read(FileConfiguration config) {
		final Map<ToolType, ToolSettings> tools = new EnumMap<>(ToolType.class);

		for(ToolType type : ToolType.values()) {
			tools.put(type, new ToolSettings(
				config.getString(type.configPath("displayName"), type.configKey()),
				config.getString(type.configPath("usage"), ""),
				Math.max(1, config.getInt(type.configPath("range"), type.defaultRange())),
				Math.max(0, config.getInt(type.configPath("cooldown"), DEFAULT_COOLDOWN_TICKS))
			));
		}

		return new Values(
			config.getString("Prefix", DEFAULT_PREFIX),
			tools,
			Math.max(1, config.getInt(ToolType.HOSE.configPath("waterLifetime"), DEFAULT_WATER_LIFETIME_TICKS))
		);
	}
}

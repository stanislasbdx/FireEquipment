package fr.stan1712.wetston.fireequipment.tools;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum ToolType {
	HOSE("Hose", Material.GOLDEN_HOE, "firequip.tools.hose", 5),
	PUMP("Pump", Material.CLAY_BALL, "firequip.tools.pump", 4),
	EXTINGUISHER("Extinguisher", Material.IRON_HOE, "firequip.tools.extinguisher", 2);

	private final String configKey;
	private final Material material;
	private final String permission;
	private final int defaultRange;

	ToolType(String configKey, Material material, String permission, int defaultRange) {
		this.configKey = configKey;
		this.material = material;
		this.permission = permission;
		this.defaultRange = defaultRange;
	}

	public String configKey() {
		return configKey;
	}

	public Material material() {
		return material;
	}

	public String permission() {
		return permission;
	}

	public int defaultRange() {
		return defaultRange;
	}

	public String argument() {
		return name().toLowerCase(Locale.ROOT);
	}

	public String configPath(String field) {
		return "Equipment." + configKey + "." + field;
	}

	public String defaultItemModel() {
		return "fireequipment:" + argument();
	}

	public String giveMessagePath() {
		return "Core.GiveMsg." + configKey;
	}

	public static Optional<ToolType> fromArgument(String argument) {
		return Arrays.stream(values()).filter(type -> type.argument().equalsIgnoreCase(argument)).findFirst();
	}

	public static Optional<ToolType> fromConfigKey(String configKey) {
		return Arrays.stream(values()).filter(type -> type.configKey.equals(configKey)).findFirst();
	}
}

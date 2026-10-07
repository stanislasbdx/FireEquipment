package fr.stan1712.wetston.fireequipment.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HexFormat;
import java.util.regex.Pattern;

public record ResourcePackSettings(boolean enabled, String url, byte[] sha1, String prompt, boolean force) {
	public static final String DEFAULT_PROMPT = "FireEquipment textures";

	private static final String PATH = "ResourcePack.";
	private static final Pattern SHA1 = Pattern.compile("[0-9a-fA-F]{40}");

	public boolean active() {
		return enabled && !url.isBlank();
	}

	public static ResourcePackSettings read(FileConfiguration config) {
		return new ResourcePackSettings(
			config.getBoolean(PATH + "enabled", false),
			config.getString(PATH + "url", "").trim(),
			parseSha1(config.getString(PATH + "sha1", "")),
			config.getString(PATH + "prompt", DEFAULT_PROMPT),
			config.getBoolean(PATH + "force", false)
		);
	}

	static byte[] parseSha1(String raw) {
		if(raw == null) return null;

		final String value = raw.trim();

		return SHA1.matcher(value).matches() ? HexFormat.of().parseHex(value) : null;
	}
}

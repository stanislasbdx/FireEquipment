package fr.stan1712.wetston.fireequipment.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourcePackSettingsTest {
	@Test
	void isDisabledByDefault() {
		final ResourcePackSettings settings = ResourcePackSettings.read(new YamlConfiguration());

		assertFalse(settings.enabled());
		assertFalse(settings.active());
		assertEquals(ResourcePackSettings.DEFAULT_PROMPT, settings.prompt());
		assertNull(settings.sha1());
	}

	@Test
	void needsAnUrlToBeActive() {
		final YamlConfiguration config = new YamlConfiguration();
		config.set("ResourcePack.enabled", true);

		assertFalse(ResourcePackSettings.read(config).active());

		config.set("ResourcePack.url", " https://example.com/pack.zip ");
		final ResourcePackSettings settings = ResourcePackSettings.read(config);

		assertTrue(settings.active());
		assertEquals("https://example.com/pack.zip", settings.url());
	}

	@Test
	void parsesTheSha1() {
		final YamlConfiguration config = new YamlConfiguration();
		config.set("ResourcePack.sha1", "DFBD1A47091542752D3AAC2DE7DB72EA2180E6DB");

		assertEquals(20, ResourcePackSettings.read(config).sha1().length);
		assertEquals((byte) 0xdf, ResourcePackSettings.read(config).sha1()[0]);
		assertNull(ResourcePackSettings.parseSha1("nope"));
		assertNull(ResourcePackSettings.parseSha1(null));
	}
}

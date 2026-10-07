package fr.stan1712.wetston.fireequipment.config;

import fr.stan1712.wetston.fireequipment.tools.ToolType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginSettingsTest {
	@Test
	void readsConfiguredValues() {
		final YamlConfiguration config = new YamlConfiguration();
		config.set("Prefix", "FE");
		config.set("Equipment.Hose.range", 8);
		config.set("Equipment.Hose.cooldown", 4);
		config.set("Equipment.Hose.waterLifetime", 100);
		config.set("Equipment.Hose.displayName", "Lance");

		final PluginSettings.Values values = PluginSettings.read(config);

		assertEquals("FE", values.prefix());
		assertEquals(8, values.tools().get(ToolType.HOSE).range());
		assertEquals(200L, values.tools().get(ToolType.HOSE).cooldownMillis());
		assertEquals(100, values.hoseWaterLifetimeTicks());
		assertEquals("Lance", values.tools().get(ToolType.HOSE).displayName());
	}

	@Test
	void fallsBackToDefaultsWhenKeysAreMissing() {
		final PluginSettings.Values values = PluginSettings.read(new YamlConfiguration());

		assertEquals(PluginSettings.DEFAULT_PREFIX, values.prefix());
		assertEquals(ToolType.EXTINGUISHER.defaultRange(), values.tools().get(ToolType.EXTINGUISHER).range());
		assertEquals(PluginSettings.DEFAULT_COOLDOWN_TICKS, values.tools().get(ToolType.PUMP).cooldownTicks());
		assertEquals(PluginSettings.DEFAULT_WATER_LIFETIME_TICKS, values.hoseWaterLifetimeTicks());
	}

	@Test
	void readsItemModels() {
		final YamlConfiguration config = new YamlConfiguration();
		config.set("Equipment.Hose.itemModel", "MyPack:Custom_Hose");
		config.set("Equipment.Pump.itemModel", "");
		config.set("Equipment.Extinguisher.itemModel", "not a key");

		final PluginSettings.Values values = PluginSettings.read(config);

		assertEquals("mypack:custom_hose", values.tools().get(ToolType.HOSE).itemModel().asString());
		assertNull(values.tools().get(ToolType.PUMP).itemModel());
		assertNull(values.tools().get(ToolType.EXTINGUISHER).itemModel());
	}

	@Test
	void defaultsItemModelsToThePackNamespace() {
		final PluginSettings.Values values = PluginSettings.read(new YamlConfiguration());

		assertEquals("fireequipment:hose", values.tools().get(ToolType.HOSE).itemModel().asString());
		assertEquals("fireequipment:extinguisher", values.tools().get(ToolType.EXTINGUISHER).itemModel().asString());
	}

	@Test
	void clampsInvalidValues() {
		final YamlConfiguration config = new YamlConfiguration();
		config.set("Equipment.Pump.range", -3);
		config.set("Equipment.Pump.cooldown", -1);

		final PluginSettings.Values values = PluginSettings.read(config);

		assertEquals(1, values.tools().get(ToolType.PUMP).range());
		assertEquals(0, values.tools().get(ToolType.PUMP).cooldownTicks());
	}
}

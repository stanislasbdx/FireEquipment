package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FireSettingsTest {
	@Test
	void usesDefaultsWithoutFactors() {
		final FireSettings settings = FireSettings.load(new YamlConfiguration(), new YamlConfiguration());

		assertEquals(FireDefaults.HEAT_GROWTH, settings.heat().growth());
		assertEquals(FireDefaults.FLASHOVER_RADIUS, settings.confinement().flashoverRadius());
		assertEquals(FireDefaults.FLASHOVER_HEAT, settings.confinement().flashoverHeat());
		assertEquals(FireDefaults.HOSE_COOLING, settings.water().hoseCooling());
		assertEquals(FireDefaults.ASH_CHANCE, settings.ash().chance());
		assertTrue(settings.features().flashover());
		assertTrue(settings.features().structure());
		assertTrue(settings.features().tnt());
	}

	@Test
	void appliesFactors() {
		final YamlConfiguration config = new YamlConfiguration();
		config.set("realisticFire.factors.speed", 2);
		config.set("realisticFire.factors.heat", 2);
		config.set("realisticFire.factors.cooling", 0.5);
		config.set("realisticFire.factors.extinguish", 0.5);
		config.set("realisticFire.factors.effects", 2);

		final FireSettings settings = FireSettings.load(config, new YamlConfiguration());

		assertEquals(FireDefaults.SPREAD_CHANCE * 2, settings.spread().baseChance());
		assertEquals(FireDefaults.HEAT_GROWTH * 2, settings.heat().growth());
		assertEquals(FireDefaults.OPEN_AIR_DISSIPATION / 2, settings.openAir().heatDissipation());
		assertEquals(FireDefaults.HOSE_COOLING / 2, settings.water().hoseCooling());
		assertEquals(FireDefaults.POWDER_COOLING / 2, settings.water().powderCooling());
		assertEquals(FireDefaults.SCORCH_CHANCE_PER_SECOND * 2, settings.scorch().chancePerSecond(), 1e-9);
		assertEquals(FireDefaults.ASH_CHANCE * 2, settings.ash().chance(), 1e-9);
		assertEquals(FireDefaults.MAX_PARTICLES_PER_CYCLE * 2, settings.ash().maxParticlesPerCycle());
		assertEquals(FireDefaults.FLASHOVER_HEAT, settings.confinement().flashoverHeat());
	}

	@Test
	void featuresSwitchMechanicsOff() {
		final YamlConfiguration config = new YamlConfiguration();
		config.set("realisticFire.features.flashover", false);
		config.set("realisticFire.features.structure", false);
		config.set("realisticFire.features.scorch", false);
		config.set("realisticFire.features.heatAura", false);

		final FireSettings settings = FireSettings.load(config, new YamlConfiguration());

		assertFalse(settings.features().flashover());
		assertFalse(settings.features().structure());
		assertFalse(settings.scorch().enabled());
		assertEquals(0, settings.ash().chance());
		assertFalse(settings.features().heatAura());
	}

	@Test
	void convertsHeatToCelsius() {
		assertEquals(FireDefaults.AMBIENT_CELSIUS, FireDefaults.celsius(0));
		assertEquals(520, FireDefaults.celsius(100));
	}
}

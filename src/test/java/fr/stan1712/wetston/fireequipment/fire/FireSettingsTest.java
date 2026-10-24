package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FireSettingsTest {
	@Test
	void usesDefaultsWithoutFactors() {
		final FireSettings settings = FireSettings.load(new YamlConfiguration());

		assertEquals(FireDefaults.HEAT_GROWTH, settings.heat().growth());
		assertEquals(FireDefaults.FLASHOVER_RADIUS, settings.confinement().flashoverRadius());
		assertEquals(FireDefaults.FLASHOVER_HEAT, settings.confinement().flashoverHeat());
		assertEquals(FireDefaults.HOSE_COOLING, settings.water().hoseCooling());
		assertEquals(FireDefaults.ASH_CHANCE, settings.ash().chance());
		assertTrue(settings.confinement().flashover());
		assertTrue(settings.features().tnt());
	}

	@Test
	void appliesFactors() {
		final YamlConfiguration config = new YamlConfiguration();
		config.set("realisticFire.factors.heat", 2);
		config.set("realisticFire.factors.flashover", 2);
		config.set("realisticFire.factors.water", 0.5);
		config.set("realisticFire.factors.heatAura", 2);
		config.set("realisticFire.factors.scorch", 3);
		config.set("realisticFire.factors.particles", 2);

		final FireSettings settings = FireSettings.load(config);

		assertEquals(FireDefaults.HEAT_GROWTH * 2, settings.heat().growth());
		assertEquals(FireDefaults.FLASHOVER_RADIUS * 2, settings.confinement().flashoverRadius());
		assertEquals(FireDefaults.FLASHOVER_HEAT / 2, settings.confinement().flashoverHeat());
		assertEquals(FireDefaults.HOSE_COOLING / 2, settings.water().hoseCooling());
		assertEquals(FireDefaults.AURA_IGNITE_THRESHOLD / 2, settings.heatAura().igniteThreshold());
		assertEquals(FireDefaults.SCORCH_CHANCE_PER_SECOND * 3, settings.scorch().chancePerSecond(), 1e-9);
		assertEquals(2, settings.ash().particleDensity());
		assertEquals(FireDefaults.MAX_PARTICLES_PER_CYCLE * 2, settings.ash().maxParticlesPerCycle());
	}

	@Test
	void featuresSwitchMechanicsOff() {
		final YamlConfiguration config = new YamlConfiguration();
		config.set("realisticFire.features.ash", false);
		config.set("realisticFire.features.flashover", false);
		config.set("realisticFire.features.heatAura", false);
		config.set("realisticFire.features.crack", false);

		final FireSettings settings = FireSettings.load(config);

		assertEquals(0, settings.ash().chance());
		assertFalse(settings.confinement().flashover());
		assertFalse(settings.heatAura().enabled());
		assertFalse(settings.features().crack());
	}

	@Test
	void convertsHeatToCelsius() {
		assertEquals(FireDefaults.AMBIENT_CELSIUS, FireDefaults.celsius(0));
		assertEquals(520, FireDefaults.celsius(100));
	}
}

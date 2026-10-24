package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record FireSettings(
		boolean enabled,
		Set<String> worlds,
		int updateInterval,
		int maxBurningBlocks,
		int maxUpdatesPerCycle,
		Heat heat,
		Spread spread,
		OpenAir openAir,
		Confinement confinement,
		Water water,
		HeatAura heatAura,
		WeakBlocks weakBlocks,
		Ash ash,
		Collapse collapse,
		Scorch scorch,
		Features features,
		Factors factors,
		Map<Material, FireCategory> categories
) {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Fire");
	private static final String ROOT = "realisticFire.";

	public record Features(boolean flashover, boolean structure, boolean scorch, boolean heatAura, boolean tnt, boolean particles) {}

	public record Factors(double speed, double heat, double cooling, double extinguish, double effects) {}

	public record Heat(double ignition, double growth, double dissipation, double extinguishThreshold, double reigniteThreshold, int reigniteDelay) {}

	public record Spread(int radius, double baseChance, double heatPerNeighbor, double upwardBonus) {}

	public record OpenAir(double burnSpeed, double heatDissipation, double rainCooling) {}

	public record Confinement(int maxRoomVolume, double burnSpeed, double heatMultiplier, double dissipation, boolean flashover, int flashoverRadius, double flashoverHeat, int flashoverCooldown, double flashoverIgniteChance, double flashoverHeatRatio) {}

	public record Water(double hoseCooling, double powderKnockdown, double powderCooling, double adjacentWaterCooling) {}

	public record HeatAura(boolean enabled, int radius, double warnThreshold, double igniteThreshold, double fireTicksPerHeat, int maxFireTicks) {}

	public record WeakBlocks(boolean enabled, int radius, double burstHeat, double burstChance, Set<Material> materials) {}

	public record Ash(double chance, WeightedMaterials materials, boolean particles, double particleDensity, int particleDuration, int maxParticlesPerCycle) {
		public Material pick() {
			return materials.pick();
		}

		public boolean isAshMaterial(Material material) {
			return materials.contains(material);
		}
	}

	public record Scorch(boolean enabled, double chancePerSecond, Set<Material> convertible, WeightedMaterials materials) {}

	public record Collapse(boolean enabled, boolean effects) {}

	public boolean isWorldEnabled(World world) {
		return enabled && worlds.contains(world.getName());
	}

	public FireCategory categoryOf(Material material) {
		return categories.get(material);
	}

	public static FireSettings load(FileConfiguration config) {
		final Features features = new Features(
				feature(config, "flashover"),
				feature(config, "structure"),
				feature(config, "scorch"),
				feature(config, "heatAura"),
				feature(config, "tnt"),
				feature(config, "particles")
		);
		final Factors factors = new Factors(
				factor(config, "speed"),
				factor(config, "heat"),
				factor(config, "cooling"),
				factor(config, "extinguish"),
				factor(config, "effects")
		);

		return new FireSettings(
				config.getBoolean(ROOT + "enabled"),
				Collections.unmodifiableSet(new HashSet<>(config.getStringList(ROOT + "worlds"))),
				Math.max(1, config.getInt(ROOT + "performance.updateInterval", FireDefaults.UPDATE_INTERVAL)),
				config.getInt(ROOT + "performance.maxBurningBlocks", FireDefaults.MAX_BURNING_BLOCKS),
				Math.max(1, config.getInt(ROOT + "performance.maxUpdatesPerCycle", FireDefaults.MAX_UPDATES_PER_CYCLE)),
				new Heat(
						FireDefaults.HEAT_IGNITION,
						FireDefaults.HEAT_GROWTH * factors.heat(),
						FireDefaults.HEAT_DISSIPATION * factors.cooling(),
						FireDefaults.EXTINGUISH_THRESHOLD,
						FireDefaults.REIGNITE_THRESHOLD,
						seconds(FireDefaults.REIGNITE_DELAY_SECONDS)
				),
				new Spread(
						FireDefaults.SPREAD_RADIUS,
						FireDefaults.SPREAD_CHANCE * factors.speed(),
						FireDefaults.HEAT_PER_NEIGHBOR,
						FireDefaults.UPWARD_BONUS
				),
				new OpenAir(
						FireDefaults.OPEN_AIR_BURN_SPEED,
						FireDefaults.OPEN_AIR_DISSIPATION * factors.cooling(),
						FireDefaults.RAIN_COOLING * factors.cooling()
				),
				new Confinement(
						FireDefaults.MAX_ROOM_VOLUME,
						FireDefaults.CONFINED_BURN_SPEED,
						FireDefaults.CONFINED_HEAT_MULTIPLIER,
						FireDefaults.CONFINED_DISSIPATION * factors.cooling(),
						features.flashover(),
						FireDefaults.FLASHOVER_RADIUS,
						FireDefaults.FLASHOVER_HEAT,
						seconds(FireDefaults.FLASHOVER_COOLDOWN_SECONDS),
						FireDefaults.FLASHOVER_IGNITE_CHANCE,
						FireDefaults.FLASHOVER_HEAT_RATIO
				),
				new Water(
						FireDefaults.HOSE_COOLING * factors.extinguish(),
						FireDefaults.POWDER_KNOCKDOWN * factors.extinguish(),
						FireDefaults.POWDER_COOLING * factors.extinguish(),
						FireDefaults.ADJACENT_WATER_COOLING * factors.extinguish()
				),
				new HeatAura(
						features.heatAura(),
						FireDefaults.AURA_RADIUS,
						FireDefaults.AURA_WARN_THRESHOLD,
						FireDefaults.AURA_IGNITE_THRESHOLD,
						FireDefaults.AURA_FIRE_TICKS_PER_HEAT,
						FireDefaults.AURA_MAX_FIRE_TICKS
				),
				new WeakBlocks(
						features.structure(),
						FireDefaults.WEAK_RADIUS,
						FireDefaults.WEAK_BURST_HEAT,
						FireDefaults.WEAK_BURST_CHANCE,
						MaterialMatcher.resolve(config.getStringList(ROOT + "weakBlocks.materials"))
				),
				new Ash(
						features.scorch() ? Math.min(1, FireDefaults.ASH_CHANCE * factors.effects()) : 0,
						WeightedMaterials.of(FireDefaults.ASH_MATERIALS, Material.GRAY_CONCRETE_POWDER),
						features.particles(),
						factors.effects(),
						seconds(FireDefaults.ASH_PARTICLE_SECONDS),
						(int) Math.round(FireDefaults.MAX_PARTICLES_PER_CYCLE * Math.max(1, factors.effects()))
				),
				new Collapse(features.structure(), features.structure()),
				new Scorch(
						features.scorch() && factors.effects() > 0,
						FireDefaults.SCORCH_CHANCE_PER_SECOND * factors.effects(),
						FireDefaults.SCORCH_CONVERTIBLE,
						WeightedMaterials.of(FireDefaults.SCORCH_MATERIALS, Material.COARSE_DIRT)
				),
				features,
				factors,
				loadCategories(config, factors)
		);
	}

	private static boolean feature(FileConfiguration config, String name) {
		return config.getBoolean(ROOT + "features." + name, true);
	}

	private static double factor(FileConfiguration config, String name) {
		return Math.max(0, config.getDouble(ROOT + "factors." + name, 1));
	}

	private static int seconds(double value) {
		return (int) Math.round(value * 20);
	}

	private static Map<Material, FireCategory> loadCategories(FileConfiguration config, Factors factors) {
		final Map<Material, FireCategory> categories = new EnumMap<>(Material.class);

		final ConfigurationSection section = config.getConfigurationSection(ROOT + "categories");
		if(section == null) {
			_log.warn("[loadCategories] No fire category defined");
			return categories;
		}

		for(String name : section.getKeys(false)) {
			final ConfigurationSection categorySection = section.getConfigurationSection(name);
			if(categorySection == null) {
				continue;
			}

			final FireCategory category = new FireCategory(
					name,
					seconds(categorySection.getDouble("burnTime") / Math.max(0.05, factors.speed())),
					categorySection.getDouble("ignitionChance"),
					categorySection.getDouble("maxHeat"),
					categorySection.getBoolean("ash", true)
			);

			for(Material material : MaterialMatcher.resolve(categorySection.getStringList("materials"))) {
				categories.put(material, category);
			}
		}

		_log.info("[loadCategories] {} flammable blocks loaded", categories.size());
		return categories;
	}
}

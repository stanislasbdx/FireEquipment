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
		Map<Material, FireCategory> categories
) {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Fire");
	private static final String ROOT = "realisticFire.";

	public record Features(boolean flashover, boolean heatAura, boolean weakBlocks, boolean collapse, boolean collapseEffects, boolean ash, boolean particles, boolean scorch, boolean tnt) {}

	public record Factors(double burnTime, double spread, double heat, double cooling, double water, double powder, double flashover, double heatAura, double weakBlocks, double ash) {}

	public record Heat(double ignition, double growth, double dissipation, double extinguishThreshold, double reigniteThreshold, int reigniteDelay) {}

	public record Spread(int radius, double baseChance, double heatPerNeighbor, double upwardBonus) {}

	public record OpenAir(double burnSpeed, double heatDissipation, double rainCooling) {}

	public record Confinement(int maxRoomVolume, double burnSpeed, double heatMultiplier, double dissipation, boolean flashover, int flashoverRadius, double flashoverHeat, int flashoverCooldown, double flashoverIgniteChance, double flashoverHeatRatio) {}

	public record Water(double hoseCooling, double powderKnockdown, double powderCooling, double adjacentWaterCooling) {}

	public record HeatAura(boolean enabled, int radius, double warnThreshold, double igniteThreshold, double fireTicksPerHeat, int maxFireTicks) {}

	public record WeakBlocks(boolean enabled, int radius, double burstHeat, double burstChance, Set<Material> materials) {}

	public record Ash(double chance, WeightedMaterials materials, boolean particles, int particleDuration, int maxParticlesPerCycle) {
		public Material pick() {
			return materials.pick();
		}

		public boolean isAshMaterial(Material material) {
			return materials.contains(material);
		}
	}

	public record Scorch(boolean enabled, Set<Material> convertible, WeightedMaterials materials) {}

	public record Collapse(boolean enabled, boolean effects, Set<Material> anchors) {}

	public boolean isWorldEnabled(World world) {
		return enabled && worlds.contains(world.getName());
	}

	public FireCategory categoryOf(Material material) {
		return categories.get(material);
	}

	public static FireSettings load(FileConfiguration config) {
		final Features features = new Features(
				feature(config, "flashover"),
				feature(config, "heatAura"),
				feature(config, "weakBlocks"),
				feature(config, "collapse"),
				feature(config, "collapseEffects"),
				feature(config, "ash"),
				feature(config, "particles"),
				feature(config, "scorch"),
				feature(config, "tnt")
		);
		final Factors factors = new Factors(
				factor(config, "burnTime"),
				factor(config, "spread"),
				factor(config, "heat"),
				factor(config, "cooling"),
				factor(config, "water"),
				factor(config, "powder"),
				factor(config, "flashover"),
				factor(config, "heatAura"),
				factor(config, "weakBlocks"),
				factor(config, "ash")
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
						FireDefaults.SPREAD_CHANCE * factors.spread(),
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
						features.flashover() && factors.flashover() > 0,
						Math.max(1, (int) Math.round(FireDefaults.FLASHOVER_RADIUS * Math.min(2, factors.flashover()))),
						FireDefaults.FLASHOVER_HEAT / Math.max(0.01, factors.flashover()),
						seconds(FireDefaults.FLASHOVER_COOLDOWN_SECONDS),
						Math.min(1, FireDefaults.FLASHOVER_IGNITE_CHANCE * factors.flashover()),
						Math.min(1, FireDefaults.FLASHOVER_HEAT_RATIO * factors.flashover())
				),
				new Water(
						FireDefaults.HOSE_COOLING * factors.water(),
						FireDefaults.POWDER_KNOCKDOWN * factors.powder(),
						FireDefaults.POWDER_COOLING * factors.powder(),
						FireDefaults.ADJACENT_WATER_COOLING * factors.water()
				),
				new HeatAura(
						features.heatAura() && factors.heatAura() > 0,
						FireDefaults.AURA_RADIUS,
						FireDefaults.AURA_WARN_THRESHOLD / Math.max(0.01, factors.heatAura()),
						FireDefaults.AURA_IGNITE_THRESHOLD / Math.max(0.01, factors.heatAura()),
						FireDefaults.AURA_FIRE_TICKS_PER_HEAT * factors.heatAura(),
						FireDefaults.AURA_MAX_FIRE_TICKS
				),
				new WeakBlocks(
						features.weakBlocks() && factors.weakBlocks() > 0,
						FireDefaults.WEAK_RADIUS,
						FireDefaults.WEAK_BURST_HEAT / Math.max(0.01, factors.weakBlocks()),
						Math.min(1, FireDefaults.WEAK_BURST_CHANCE * factors.weakBlocks()),
						MaterialMatcher.resolve(config.getStringList(ROOT + "weakBlocks.materials"))
				),
				loadAsh(config, features, factors),
				new Collapse(
						features.collapse(),
						features.collapseEffects(),
						MaterialMatcher.resolve(config.getStringList(ROOT + "collapse.anchors"))
				),
				new Scorch(
						features.scorch(),
						MaterialMatcher.resolve(config.getStringList(ROOT + "scorch.convertible")),
						WeightedMaterials.read(config.getConfigurationSection(ROOT + "scorch.materials"), Material.COARSE_DIRT)
				),
				features,
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

	private static Ash loadAsh(FileConfiguration config, Features features, Factors factors) {
		return new Ash(
				features.ash() ? Math.min(1, FireDefaults.ASH_CHANCE * factors.ash()) : 0,
				WeightedMaterials.read(config.getConfigurationSection(ROOT + "ash.materials"), Material.GRAY_CONCRETE_POWDER),
				features.particles(),
				seconds(FireDefaults.ASH_PARTICLE_SECONDS),
				FireDefaults.MAX_PARTICLES_PER_CYCLE
		);
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
					seconds(categorySection.getDouble("burnTime") * Math.max(0.05, factors.burnTime())),
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

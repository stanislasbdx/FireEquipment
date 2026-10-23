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
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

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
		Map<Material, FireCategory> categories
) {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Fire");
	private static final String ROOT = "realisticFire.";

	public record Heat(double ignition, double growth, double dissipation, double extinguishThreshold, double reigniteThreshold, int reigniteDelay) {}

	public record Spread(int radius, double baseChance, double heatPerNeighbor, double upwardBonus) {}

	public record OpenAir(double burnSpeed, double heatDissipation, double rainCooling) {}

	public record Confinement(int maxRoomVolume, double burnSpeed, double heatMultiplier, int flashoverRadius, double flashoverHeat, int flashoverCooldown) {}

	public record Water(double hoseCooling, double powderKnockdown, double powderCooling, double adjacentWaterCooling) {}

	public record HeatAura(boolean enabled, int radius, double warnThreshold, double igniteThreshold, double fireTicksPerHeat, int maxFireTicks) {}

	public record WeakBlocks(boolean enabled, int radius, double burstHeat, double burstChance, Set<Material> materials) {}

	public record Ash(double chance, NavigableMap<Integer, Material> weightedMaterials, int totalWeight, boolean particles, int particleDuration, int maxParticlesPerCycle) {
		public Material pick() {
			if(totalWeight <= 0) {
				return Material.GRAY_CONCRETE_POWDER;
			}
			return weightedMaterials.ceilingEntry(ThreadLocalRandom.current().nextInt(totalWeight)).getValue();
		}

		public boolean isAshMaterial(Material material) {
			return weightedMaterials.containsValue(material);
		}
	}

	public record Collapse(boolean enabled, int maxHeight, int minSideSupport, Set<Material> excluded) {}

	public boolean isWorldEnabled(World world) {
		return enabled && worlds.contains(world.getName());
	}

	public FireCategory categoryOf(Material material) {
		return categories.get(material);
	}

	public static FireSettings load(FileConfiguration config) {
		return new FireSettings(
				config.getBoolean(ROOT + "enabled"),
				Collections.unmodifiableSet(new HashSet<>(config.getStringList(ROOT + "worlds"))),
				Math.max(1, config.getInt(ROOT + "updateInterval")),
				config.getInt(ROOT + "maxBurningBlocks"),
				Math.max(1, config.getInt(ROOT + "maxUpdatesPerCycle")),
				new Heat(
						config.getDouble(ROOT + "heat.ignition"),
						config.getDouble(ROOT + "heat.growth"),
						config.getDouble(ROOT + "heat.dissipation"),
						config.getDouble(ROOT + "heat.extinguishThreshold"),
						config.getDouble(ROOT + "heat.reigniteThreshold"),
						seconds(config.getDouble(ROOT + "heat.reigniteDelay"))
				),
				new Spread(
						config.getInt(ROOT + "spread.radius"),
						config.getDouble(ROOT + "spread.baseChance"),
						config.getDouble(ROOT + "spread.heatPerNeighbor"),
						config.getDouble(ROOT + "spread.upwardBonus")
				),
				new OpenAir(
						config.getDouble(ROOT + "openAir.burnSpeed"),
						config.getDouble(ROOT + "openAir.heatDissipation"),
						config.getDouble(ROOT + "openAir.rainCooling")
				),
				new Confinement(
						config.getInt(ROOT + "confinement.maxRoomVolume"),
						config.getDouble(ROOT + "confinement.burnSpeed"),
						config.getDouble(ROOT + "confinement.heatMultiplier"),
						config.getInt(ROOT + "confinement.flashoverRadius"),
						config.getDouble(ROOT + "confinement.flashoverHeat"),
						seconds(config.getDouble(ROOT + "confinement.flashoverCooldown"))
				),
				new Water(
						config.getDouble(ROOT + "water.hoseCooling"),
						config.getDouble(ROOT + "water.powderKnockdown"),
						config.getDouble(ROOT + "water.powderCooling"),
						config.getDouble(ROOT + "water.adjacentWaterCooling")
				),
				new HeatAura(
						config.getBoolean(ROOT + "heatAura.enabled"),
						config.getInt(ROOT + "heatAura.radius"),
						config.getDouble(ROOT + "heatAura.warnThreshold"),
						config.getDouble(ROOT + "heatAura.igniteThreshold"),
						config.getDouble(ROOT + "heatAura.fireTicksPerHeat"),
						config.getInt(ROOT + "heatAura.maxFireTicks")
				),
				new WeakBlocks(
						config.getBoolean(ROOT + "weakBlocks.enabled"),
						config.getInt(ROOT + "weakBlocks.radius"),
						config.getDouble(ROOT + "weakBlocks.burstHeat"),
						config.getDouble(ROOT + "weakBlocks.burstChance"),
						MaterialMatcher.resolve(config.getStringList(ROOT + "weakBlocks.materials"))
				),
				loadAsh(config),
				new Collapse(
						config.getBoolean(ROOT + "collapse.enabled"),
						config.getInt(ROOT + "collapse.maxHeight"),
						config.getInt(ROOT + "collapse.minSideSupport"),
						MaterialMatcher.resolve(config.getStringList(ROOT + "collapse.excluded"))
				),
				loadCategories(config)
		);
	}

	private static int seconds(double value) {
		return (int) Math.round(value * 20);
	}

	private static Ash loadAsh(FileConfiguration config) {
		final NavigableMap<Integer, Material> weighted = new TreeMap<>();
		int total = 0;

		final ConfigurationSection section = config.getConfigurationSection(ROOT + "ash.materials");
		if(section != null) {
			for(String name : section.getKeys(false)) {
				final Material material = Material.matchMaterial(name);
				final int weight = section.getInt(name);

				if(material == null || !material.isBlock() || weight <= 0) {
					_log.warn("[loadAsh] Invalid ash material {}", name);
					continue;
				}
				total += weight;
				weighted.put(total - 1, material);
			}
		}

		return new Ash(
				config.getDouble(ROOT + "ash.chance"),
				weighted,
				total,
				config.getBoolean(ROOT + "ash.particles"),
				seconds(config.getDouble(ROOT + "ash.particleDuration")),
				config.getInt(ROOT + "ash.maxParticlesPerCycle")
		);
	}

	private static Map<Material, FireCategory> loadCategories(FileConfiguration config) {
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
					seconds(categorySection.getDouble("burnTime")),
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

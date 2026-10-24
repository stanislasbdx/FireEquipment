package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public record WeightedMaterials(NavigableMap<Integer, Material> weighted, int total, Material fallback) {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Fire");

	public static WeightedMaterials read(ConfigurationSection section, Material fallback) {
		final NavigableMap<Integer, Material> weighted = new TreeMap<>();
		int total = 0;

		if(section != null) {
			for(String name : section.getKeys(false)) {
				final Material material = Material.matchMaterial(name);
				final int weight = section.getInt(name);

				if(material == null || !material.isBlock() || weight <= 0) {
					_log.warn("[WeightedMaterials] Invalid material {} in {}", name, section.getCurrentPath());
					continue;
				}
				total += weight;
				weighted.put(total - 1, material);
			}
		}

		return new WeightedMaterials(weighted, total, fallback);
	}

	public Material pick() {
		if(total <= 0) {
			return fallback;
		}
		return weighted.ceilingEntry(ThreadLocalRandom.current().nextInt(total)).getValue();
	}

	public boolean contains(Material material) {
		return total <= 0 ? material == fallback : weighted.containsValue(material);
	}
}

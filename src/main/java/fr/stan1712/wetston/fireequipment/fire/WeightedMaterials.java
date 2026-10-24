package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Material;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public record WeightedMaterials(NavigableMap<Integer, Material> weighted, int total, Material fallback) {
	public static WeightedMaterials of(Map<Material, Integer> weights, Material fallback) {
		final NavigableMap<Integer, Material> weighted = new TreeMap<>();
		int total = 0;

		for(Map.Entry<Material, Integer> entry : weights.entrySet()) {
			total += entry.getValue();
			weighted.put(total - 1, entry.getKey());
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

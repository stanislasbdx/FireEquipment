package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MaterialMatcher {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Fire");

	private MaterialMatcher() {
		throw new IllegalStateException("Utility class");
	}

	public static Set<Material> resolve(List<String> entries) {
		final Set<Material> materials = EnumSet.noneOf(Material.class);

		for(String rawEntry : entries) {
			final String entry = rawEntry.trim();

			if(entry.startsWith("#")) {
				final NamespacedKey key = NamespacedKey.fromString(entry.substring(1).toLowerCase(Locale.ROOT));
				final Tag<Material> tag = key == null ? null : Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class);

				if(tag == null) {
					_log.warn("[MaterialMatcher] Unknown block tag {}", entry);
					continue;
				}
				materials.addAll(tag.getValues());
			}
			else if(entry.contains("*")) {
				final String name = entry.toUpperCase(Locale.ROOT).replaceFirst("^MINECRAFT:", "");
				final Pattern pattern = Pattern.compile(Arrays.stream(name.split("\\*", -1)).map(Pattern::quote).collect(Collectors.joining(".*")));
				final List<Material> matched = Arrays.stream(Material.values())
						.filter(material -> !material.isLegacy() && material.isBlock() && pattern.matcher(material.name()).matches())
						.toList();

				if(matched.isEmpty()) {
					_log.warn("[MaterialMatcher] No block matches {}", entry);
				}
				materials.addAll(matched);
			}
			else {
				final Material material = Material.matchMaterial(entry);

				if(material == null || !material.isBlock()) {
					_log.warn("[MaterialMatcher] Unknown block {}", entry);
					continue;
				}
				materials.add(material);
			}
		}

		return materials;
	}
}

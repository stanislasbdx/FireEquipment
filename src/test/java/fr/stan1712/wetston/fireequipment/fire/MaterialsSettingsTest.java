package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaterialsSettingsTest {
	@BeforeEach
	void setUp() {
		MockBukkit.mock();
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void readsCategoriesAndWeakBlocksFromMaterials() {
		final YamlConfiguration materials = new YamlConfiguration();
		materials.set("weakBlocks", List.of("GLASS"));
		materials.set("categories.wood.burnTime", 30);
		materials.set("categories.wood.maxHeat", 100);
		materials.set("categories.wood.materials", List.of("OAK_PLANKS"));

		final FireSettings settings = FireSettings.load(new YamlConfiguration(), materials);

		assertTrue(settings.weakBlocks().materials().contains(Material.GLASS));
		assertEquals("wood", settings.categoryOf(Material.OAK_PLANKS).name());
		assertEquals(600, settings.categoryOf(Material.OAK_PLANKS).burnTicks());
	}

	@Test
	void fallsBackToDefaultCategoriesWhenMissing() {
		final YamlConfiguration defaults = new YamlConfiguration();
		defaults.set("categories.paper.burnTime", 20);
		defaults.set("categories.paper.materials", List.of("BOOKSHELF"));
		final YamlConfiguration materials = new YamlConfiguration();
		materials.setDefaults(defaults);

		final FireSettings settings = FireSettings.load(new YamlConfiguration(), materials);

		assertEquals("paper", settings.categoryOf(Material.BOOKSHELF).name());
	}
}

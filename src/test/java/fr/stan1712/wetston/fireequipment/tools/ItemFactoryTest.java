package fr.stan1712.wetston.fireequipment.tools;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ItemFactoryTest {
	private Plugin plugin;
	private ItemFactory items;

	@BeforeEach
	void setUp() {
		MockBukkit.mock();
		plugin = MockBukkit.createMockPlugin();
		items = new ItemFactory(plugin, new PluginSettings(plugin));
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	private ItemStack tagged(Material material, String value) {
		final ItemStack item = new ItemStack(material);
		final ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "firequip"), PersistentDataType.STRING, value);
		item.setItemMeta(meta);

		return item;
	}

	@Test
	void identifiesLegacyTaggedItems() {
		assertEquals(Optional.of(ToolType.HOSE), items.identify(tagged(Material.GOLDEN_HOE, "item-type-Hose")));
		assertEquals(Optional.of(ToolType.PUMP), items.identify(tagged(Material.CLAY_BALL, "item-type-Pump")));
		assertEquals(Optional.of(ToolType.EXTINGUISHER), items.identify(tagged(Material.IRON_HOE, "item-type-Extinguisher")));
	}

	@Test
	void identificationIgnoresNameLoreAndMaterial() {
		final ItemStack renamed = tagged(Material.STICK, "item-type-Hose");
		final ItemMeta meta = renamed.getItemMeta();
		meta.setDisplayName("Whatever");
		renamed.setItemMeta(meta);

		assertEquals(Optional.of(ToolType.HOSE), items.identify(renamed));
	}

	@Test
	void plainOrForeignItemsAreNotTools() {
		assertTrue(items.identify(null).isEmpty());
		assertTrue(items.identify(new ItemStack(Material.GOLDEN_HOE)).isEmpty());
		assertTrue(items.identify(tagged(Material.GOLDEN_HOE, "something-else")).isEmpty());
		assertTrue(items.identify(tagged(Material.GOLDEN_HOE, "item-type-Sword")).isEmpty());
	}

	@Test
	void createdItemsRoundTrip() {
		for(ToolType type : ToolType.values()) {
			final ItemStack item = items.create(type);

			assertEquals(type.material(), item.getType());
			assertEquals(Optional.of(type), items.identify(item));
		}
	}

	@Test
	void applyModelSetsTheConfiguredModel() {
		for(ToolType type : ToolType.values()) {
			final ItemMeta meta = new ItemStack(type.material()).getItemMeta();

			assertTrue(items.needsModel(meta, type));
			items.applyModel(meta, type);

			assertEquals(type.defaultItemModel(), meta.getItemModel().asString());
			assertFalse(items.needsModel(meta, type));
		}
	}

	@Test
	void refreshUpdatesLegacyItemsAndKeepsTheirTag() {
		final ItemStack legacy = tagged(Material.GOLDEN_HOE, "item-type-Hose");

		assertTrue(items.refresh(legacy));
		assertEquals(Optional.of(ToolType.HOSE), items.identify(legacy));
	}

	@Test
	void refreshIgnoresForeignItems() {
		final ItemStack plain = new ItemStack(Material.GOLDEN_HOE);

		assertFalse(items.refresh(plain));
		assertFalse(plain.getItemMeta().hasItemModel());
	}
}

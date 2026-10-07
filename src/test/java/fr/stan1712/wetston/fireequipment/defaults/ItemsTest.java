package fr.stan1712.wetston.fireequipment.defaults;

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

import static org.junit.jupiter.api.Assertions.*;

class ItemsTest {
	private Plugin plugin;
	private Items items;

	@BeforeEach
	void setUp() {
		MockBukkit.mock();
		plugin = MockBukkit.createMockPlugin();
		items = new Items(plugin);
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
	void identifiesToolsByPersistentData() {
		assertEquals(Items.HOSE, items.identify(tagged(Material.GOLDEN_HOE, "item-type-Hose")));
		assertEquals(Items.PUMP, items.identify(tagged(Material.CLAY_BALL, "item-type-Pump")));
		assertEquals(Items.EXTINGUISHER, items.identify(tagged(Material.IRON_HOE, "item-type-Extinguisher")));
	}

	@Test
	void identificationIgnoresNameLoreAndMaterial() {
		final ItemStack renamed = tagged(Material.STICK, "item-type-Hose");
		final ItemMeta meta = renamed.getItemMeta();
		meta.setDisplayName("Whatever");
		renamed.setItemMeta(meta);

		assertEquals(Items.HOSE, items.identify(renamed));
	}

	@Test
	void plainOrForeignItemsAreNotTools() {
		assertNull(items.identify(null));
		assertNull(items.identify(new ItemStack(Material.GOLDEN_HOE)));
		assertNull(items.identify(tagged(Material.GOLDEN_HOE, "something-else")));
	}
}

package fr.stan1712.wetston.fireequipment.defaults;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static fr.stan1712.wetston.fireequipment.utils.Utils.ConfigFactory.getConfigString;

public class Items {
	public static final String PDC_KEY = "firequip";

	private final Plugin pl;
	private final NamespacedKey namespacedKey;

	public Items(Plugin pl) {
		this.pl = pl;
		this.namespacedKey = new NamespacedKey(this.pl, PDC_KEY);
	}

	public boolean isEquipment(ItemStack stack, String itemConfigName) {
		if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) {
			return false;
		}
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) {
			return false;
		}
		String value = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
		return ("item-type-" + itemConfigName).equals(value);
	}

	private ItemStack makeNewItem(Material fallback, String itemConfigName) {
		ItemStack item = new ItemStack(readMaterial(itemConfigName, fallback));
		ItemMeta meta = item.getItemMeta();

		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.WHITE + "» " + getConfigString("Equipment." + itemConfigName + ".usage"));

		assert meta != null;
		PersistentDataContainer data = meta.getPersistentDataContainer();
		data.set(namespacedKey, PersistentDataType.STRING, "item-type-" + itemConfigName);

		applyCustomModelData(meta, readCustomModelData(itemConfigName));
		meta.setUnbreakable(true);

		meta.setDisplayName(ChatColor.RED + "§l" + getConfigString("Equipment." + itemConfigName + ".displayName"));
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}

	private void applyCustomModelData(ItemMeta meta, int value) {
		if (value <= 0) {
			return;
		}
		meta.setCustomModelData(value);
		CustomModelDataComponent component = meta.getCustomModelDataComponent();
		if (component == null) {
			return;
		}
		component.setFloats(List.of((float) value));
		meta.setCustomModelDataComponent(component);
	}

	private int readCustomModelData(String itemConfigName) {
		String base = "Equipment." + itemConfigName + ".";
		if (pl.getConfig().contains(base + "custom-model-data")) {
			return pl.getConfig().getInt(base + "custom-model-data");
		}
		if (pl.getConfig().contains(base + "CustomModelData")) {
			return pl.getConfig().getInt(base + "CustomModelData");
		}
		if (pl.getConfig().contains(base + "custom_model_data")) {
			return pl.getConfig().getInt(base + "custom_model_data");
		}
		return 1;
	}

	private Material readMaterial(String itemConfigName, Material fallback) {
		String raw = pl.getConfig().getString("Equipment." + itemConfigName + ".material");
		if (raw == null || raw.isBlank()) {
			return fallback;
		}
		try {
			return Material.valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException exception) {
			return fallback;
		}
	}

	public ItemStack getHoseItem() {
		return makeNewItem(Material.GOLDEN_HOE, "Hose");
	}

	public ItemStack getPumpItem() {
		return makeNewItem(Material.CLAY_BALL, "Pump");
	}

	public ItemStack getExtinguisherItem() {
		return makeNewItem(Material.IRON_HOE, "Extinguisher");
	}
}

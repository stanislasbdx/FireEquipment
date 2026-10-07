package fr.stan1712.wetston.fireequipment.defaults;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

import static fr.stan1712.wetston.fireequipment.utils.Utils.ConfigFactory.getConfigString;

public class Items {
	public static final String HOSE = "Hose";
	public static final String PUMP = "Pump";
	public static final String EXTINGUISHER = "Extinguisher";

	private static final String TYPE_PREFIX = "item-type-";

	private Plugin pl;

	private NamespacedKey namespacedKey = null;

	public Items(Plugin pl) {
		this.pl = pl;
		pl.getConfig();

		namespacedKey = new NamespacedKey(this.pl, "firequip");
	}
	
	private ItemStack makeNewItem(Material material, String itemConfigName) {
		ItemStack hose = new ItemStack(material);
		ItemMeta meta = hose.getItemMeta();

		ArrayList<String> hoseLore = new ArrayList<>();
		hoseLore.add(ChatColor.WHITE + "» " + getConfigString("Equipment." + itemConfigName + ".usage"));

		assert meta != null;
		PersistentDataContainer hoseData = meta.getPersistentDataContainer();

		hoseData.set(namespacedKey, PersistentDataType.STRING, TYPE_PREFIX + itemConfigName);

		meta.addEnchant(Enchantment.FIRE_PROTECTION, 1, true);
		meta.setUnbreakable(true);

		meta.setDisplayName(ChatColor.RED + "§l" + getConfigString("Equipment." + itemConfigName + ".displayName"));
		meta.setLore(hoseLore);
		hose.setItemMeta(meta);

		return hose;
	}

	public String identify(ItemStack item) {
		if(item == null || !item.hasItemMeta()) return null;

		final String value = item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
		if(value == null || !value.startsWith(TYPE_PREFIX)) return null;

		return value.substring(TYPE_PREFIX.length());
	}

	public ItemStack getHoseItem() {
		return makeNewItem(Material.GOLDEN_HOE, HOSE);
	}

	public ItemStack getPumpItem() {
		return makeNewItem(Material.CLAY_BALL, PUMP);
	}

	public ItemStack getExtinguisherItem() {
		return makeNewItem(Material.IRON_HOE, EXTINGUISHER);
	}
}

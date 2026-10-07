package fr.stan1712.wetston.fireequipment.tools;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.config.PluginSettings.ToolSettings;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ItemFactory {
	private static final String TYPE_PREFIX = "item-type-";

	private final PluginSettings settings;
	private final NamespacedKey namespacedKey;

	public ItemFactory(Plugin plugin, PluginSettings settings) {
		this.settings = settings;
		this.namespacedKey = new NamespacedKey(plugin, "firequip");
	}

	public ItemStack create(ToolType type) {
		final ToolSettings tool = settings.tool(type);
		final ItemStack item = new ItemStack(type.material());
		final ItemMeta meta = item.getItemMeta();

		meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, TYPE_PREFIX + type.configKey());
		meta.addEnchant(Enchantment.FIRE_PROTECTION, 1, true);
		meta.setUnbreakable(true);
		applyModel(meta, type);
		meta.setDisplayName(ChatColor.RED + "§l" + Messages.legacy(tool.displayName()));
		meta.setLore(List.of(ChatColor.WHITE + "» " + Messages.legacy(tool.usage())));
		item.setItemMeta(meta);

		return item;
	}

	public boolean refresh(ItemStack item) {
		final Optional<ToolType> type = identify(item);
		if(type.isEmpty()) return false;

		final ItemMeta meta = item.getItemMeta();
		if(!needsModel(meta, type.get())) return false;

		applyModel(meta, type.get());
		item.setItemMeta(meta);
		return true;
	}

	void applyModel(ItemMeta meta, ToolType type) {
		meta.setItemModel(settings.tool(type).itemModel());
	}

	boolean needsModel(ItemMeta meta, ToolType type) {
		final NamespacedKey current = meta.hasItemModel() ? meta.getItemModel() : null;

		return !Objects.equals(settings.tool(type).itemModel(), current);
	}

	public void refresh(Player player) {
		final PlayerInventory inventory = player.getInventory();

		for(int slot = 0; slot < inventory.getSize(); slot++) {
			final ItemStack item = inventory.getItem(slot);
			if(item != null && refresh(item)) inventory.setItem(slot, item);
		}
	}

	public Optional<ToolType> identify(ItemStack item) {
		if(item == null || !item.hasItemMeta()) return Optional.empty();

		final String value = item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
		if(value == null || !value.startsWith(TYPE_PREFIX)) return Optional.empty();

		return ToolType.fromConfigKey(value.substring(TYPE_PREFIX.length()));
	}
}

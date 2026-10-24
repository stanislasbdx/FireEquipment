package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HeatAura {
	private final FireManager fire;

	public HeatAura(FireManager fire) {
		this.fire = fire;
	}

	public void tick() {
		final FireSettings.HeatAura aura = fire.getSettings().heatAura();
		if(!fire.getSettings().features().heatAura() || fire.all().isEmpty()) {
			return;
		}

		for(World world : Bukkit.getWorlds()) {
			if(!fire.isWorldEnabled(world)) {
				continue;
			}

			for(Player player : world.getPlayers()) {
				if(player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
					expose(player, aura);
				}
			}
		}
	}

	private void expose(Player player, FireSettings.HeatAura aura) {
		final Location location = player.getLocation().add(0, 1, 0);
		final double heat = fire.heatAround(player.getWorld().getUID(), location.getX(), location.getY(), location.getZ(), aura.radius(), true);

		if(heat >= aura.igniteThreshold()) {
			int fireTicks = (int) Math.min(aura.maxFireTicks(), Math.max(20, (heat - aura.igniteThreshold()) * aura.fireTicksPerHeat()));
			fireTicks -= (int) (fireTicks * Math.min(1, fireProtection(player) * 0.15));

			if(fireTicks > player.getFireTicks()) {
				player.setFireTicks(fireTicks);
			}
		}
		else if(heat >= aura.warnThreshold()) {
			player.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 0.4f, 1.4f);
		}
	}

	private static int fireProtection(Player player) {
		int level = 0;

		for(ItemStack armor : player.getInventory().getArmorContents()) {
			if(armor != null) {
				level += armor.getEnchantmentLevel(Enchantment.FIRE_PROTECTION);
			}
		}

		return level;
	}
}

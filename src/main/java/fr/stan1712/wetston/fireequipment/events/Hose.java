package fr.stan1712.wetston.fireequipment.events;

import fr.stan1712.wetston.fireequipment.Main;
import fr.stan1712.wetston.fireequipment.defaults.Items;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static fr.stan1712.wetston.fireequipment.utils.Utils.ConfigFactory.getConfigString;

public class Hose implements Listener {
	private static final long DEFAULT_WATER_LIFETIME = 60L;

	private final Set<Block> placedWater = new HashSet<>();
	private Main pl;

	public Hose(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Items items = new Items(this.pl);

		if(ToolSupport.isMainHandRightClick(event)) {
			if(Items.HOSE.equals(items.identify(player.getInventory().getItemInMainHand()))) {
				if(player.hasPermission("firequip.tools.hose")) {
					Location loc = player.getEyeLocation();
					World world = player.getWorld();

					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15, 10));

					for(double d = 0; d <= this.pl.getConfig().getInt("Equipment.Hose.range"); d += 1){
						loc.add(loc.getDirection());
						world.playSound(loc, Sound.ENTITY_DOLPHIN_SPLASH, 5, 5);
						world.spawnParticle(Particle.SPLASH, loc, 100);
						world.spawnParticle(Particle.FALLING_WATER, loc, 65);

						double random = Math.random();
						if(d >= 1 && random >= 0.2) {
							final Block block = world.getBlockAt(loc);
							if(block.isEmpty() || block.getType().equals(Material.FIRE)) {
								if(!ToolSupport.canPlace(player, block)) break;

								block.setType(Material.WATER);
								scheduleWaterRemoval(block);
							}
							else {
								break;
							}
						}
					}
				} else {
					player.sendMessage("[" + getConfigString("Prefix") + "]" + getConfigString("Core.NoPerms"));
				}

				event.setCancelled(true);
			}
		}
	}

	private void scheduleWaterRemoval(Block block) {
		placedWater.add(block);

		final long lifetime = this.pl.getConfig().getLong("Equipment.Hose.waterLifetime", DEFAULT_WATER_LIFETIME);
		Bukkit.getScheduler().runTaskLater(this.pl, () -> removeWater(block), lifetime);
	}

	private void removeWater(Block block) {
		if(placedWater.remove(block) && block.getType() == Material.WATER) {
			block.setType(Material.AIR);
		}
	}

	public void removeAllWater() {
		new ArrayList<>(placedWater).forEach(this::removeWater);
	}
}

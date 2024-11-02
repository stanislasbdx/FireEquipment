package fr.stan1712.wetston.fireequipment.events;

import fr.stan1712.wetston.fireequipment.Main;
import fr.stan1712.wetston.fireequipment.defaults.Items;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static fr.stan1712.wetston.fireequipment.Utils.ConfigFactory.getConfigString;

public class Hose implements Listener {
	private Main pl;

	public Hose(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Items items = new Items(this.pl);

		if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			ItemStack hose = items.getHoseItem();

			if(player.getInventory().getItemInMainHand().equals(hose)) {
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
							if(world.getBlockAt(loc).isEmpty() || world.getBlockAt(loc).getType().equals(Material.FIRE)) {
								world.getBlockAt(loc).setType(Material.WATER);
							}
							else {
								d = d + this.pl.getConfig().getInt("Equipment.Hose.range");
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
}

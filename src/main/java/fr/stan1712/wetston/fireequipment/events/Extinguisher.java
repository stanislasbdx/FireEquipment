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

public class Extinguisher implements Listener {
	private Main pl;

	public Extinguisher(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Items items = new Items(this.pl);

		if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			ItemStack extinguisher = items.getExtinguisherItem();

			if(player.getInventory().getItemInMainHand().equals(extinguisher)) {
				if(player.hasPermission("firequip.tools.extinguisher")) {
					Location loc = player.getEyeLocation();
					World world = player.getWorld();


					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15, 1));

					for(double d = 0; d <= this.pl.getConfig().getInt("Equipment.Hose.range"); d += 1){
						loc.add(loc.getDirection());
						world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 3, 10);
						world.spawnParticle(Particle.FALLING_WATER, loc, 4);

						double random = Math.random();
						if(d >= 1) {
							if(random >= 0.5 && world.getBlockAt(loc).getType().equals(Material.FIRE)) {
								world.getBlockAt(loc).setType(Material.AIR);
							}
						}
					}
				} else {
					player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
				}

				event.setCancelled(true);
			}
		}
	}
}

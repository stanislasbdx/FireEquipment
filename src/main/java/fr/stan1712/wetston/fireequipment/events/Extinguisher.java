package fr.stan1712.wetston.fireequipment.events;

import fr.stan1712.wetston.fireequipment.Main;
import fr.stan1712.wetston.fireequipment.defaults.Items;
import fr.stan1712.wetston.fireequipment.utils.Cooldowns;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static fr.stan1712.wetston.fireequipment.utils.Utils.ConfigFactory.getConfigString;

public class Extinguisher implements Listener {
	private final Cooldowns cooldowns = new Cooldowns();
	private Main pl;

	public Extinguisher(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Items items = new Items(this.pl);

		if(ToolSupport.isMainHandRightClick(event)) {
			if(Items.EXTINGUISHER.equals(items.identify(player.getInventory().getItemInMainHand()))) {
				if(player.hasPermission("firequip.tools.extinguisher")) {
					final long cooldownMillis = this.pl.getConfig().getLong("Equipment.Extinguisher.cooldown", 10L) * 50L;
					if(!cooldowns.tryUse(player.getUniqueId(), System.currentTimeMillis(), cooldownMillis)) {
						event.setCancelled(true);
						return;
					}

					Location loc = player.getEyeLocation();
					World world = player.getWorld();


					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15, 1));

					for(double d = 0; d <= this.pl.getConfig().getInt("Equipment.Extinguisher.range"); d += 1){
						loc.add(loc.getDirection());
						world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 3, 10);
						world.spawnParticle(Particle.FALLING_WATER, loc, 4);

						double random = Math.random();
						final Block block = world.getBlockAt(loc);
						if(d >= 1 && random >= 0.5 && block.getType().equals(Material.FIRE) && ToolSupport.canBreak(player, block)) {
							block.setType(Material.AIR);
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

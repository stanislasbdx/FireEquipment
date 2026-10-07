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

import static fr.stan1712.wetston.fireequipment.utils.Utils.ConfigFactory.getConfigString;

public class Pump implements Listener {
	private final Cooldowns cooldowns = new Cooldowns();
	private final Main pl;

	public Pump(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Items items = new Items(this.pl);

		if(ToolSupport.isMainHandRightClick(event)) {
			if(Items.PUMP.equals(items.identify(player.getInventory().getItemInMainHand()))) {
				if(player.hasPermission("firequip.tools.pump")) {
					final long cooldownMillis = this.pl.getConfig().getLong("Equipment.Pump.cooldown", 10L) * 50L;
					if(!cooldowns.tryUse(player.getUniqueId(), System.currentTimeMillis(), cooldownMillis)) {
						event.setCancelled(true);
						return;
					}

					Location loc = player.getEyeLocation();
					World world = player.getWorld();

					for(double d = 0; d <= this.pl.getConfig().getInt("Equipment.Pump.range"); d += 0.75){
						loc.add(loc.getDirection());
						world.playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, (float) 0.5, -2);
						world.spawnParticle(Particle.ASH, loc, 10);

						final Block block = world.getBlockAt(loc);
						if(block.getType() == Material.WATER && ToolSupport.canBreak(player, block)) {
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

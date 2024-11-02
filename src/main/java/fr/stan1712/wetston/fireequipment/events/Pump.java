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

public class Pump implements Listener {
	private Main pl;

	public Pump(Main pl) {
		this.pl = pl;
		pl.getConfig();
	}

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Items items = new Items(this.pl);

		if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			ItemStack pump = items.getPumpItem();

			if(player.getInventory().getItemInMainHand().equals(pump)) {
				if(player.hasPermission("firequip.tools.pump")) {
					Location loc = player.getEyeLocation();
					World world = player.getWorld();

					for(double d = 0; d <= this.pl.getConfig().getInt("Equipment.Pump.range"); d += 0.75){
						loc.add(loc.getDirection());
						world.playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, (float) 0.5, -2);
						world.spawnParticle(Particle.ASH, loc, 10);

						if(world.getBlockAt(loc).isLiquid()) {
							world.getBlockAt(loc).setType(Material.AIR);
						}
					}
				} else {
					player.sendMessage("[" + this.pl.getConfig().getString("Prefix").replace("&", "§") + "]" + this.pl.getConfig().getString("Core.NoPerms").replace("&", "§"));
				}
			}
		}
	}
}

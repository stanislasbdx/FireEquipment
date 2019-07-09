package fr.stan1712.firequip;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Events implements Listener {
    @SuppressWarnings("unused")
	private Main pl;
    
    public Events(Main pl) {
        this.pl = pl;
        pl.getConfig();
    }
    
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
        	
        	// lance a eau
        	if(player.hasPermission("firequip.hose")) {
	        	Location loc = player.getEyeLocation();
	            World world = player.getWorld();
	            double maxLength = 5;
	            for(double d = 0; d <= maxLength; d += 0.5){
	                loc.add(loc.getDirection());
	                world.spawnParticle(Particle.WATER_SPLASH, loc, 100);
	            }
        	}
	    }
    }
	
}

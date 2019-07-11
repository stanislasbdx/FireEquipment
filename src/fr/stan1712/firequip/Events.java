package fr.stan1712.firequip;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Events implements Listener {
	private Main pl;
    
    public Events(Main pl) {
        this.pl = pl;
        pl.getConfig();
    }
    
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
        	
        	// lance a eau
	        ItemStack hose = new ItemStack(Material.GOLD_HOE);
	        ItemMeta meta = hose.getItemMeta();

	        meta.addEnchant(Enchantment.PROTECTION_FIRE, 10, true);
	        meta.setDisplayName(ChatColor.RED + this.pl.getConfig().getString("Equipment.Hose.displayName").replace("&", "§"));
	        hose.setItemMeta(meta);
	        
    		if(player.getInventory().getItemInMainHand().equals(hose)) {
    			if(player.hasPermission("firequip.tools.hose")) {
    	        	Location loc = player.getEyeLocation();
    	            World world = player.getWorld();
    	            
    	            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 15, 10));
    	            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 15, 999));
    	            
    	            for(double d = 0; d <= this.pl.getConfig().getInt("Equipment.Hose.range"); d += 1){
    	                loc.add(loc.getDirection());
    	                world.playSound(loc, Sound.BLOCK_CLOTH_BREAK, 5, 5);
    	                world.spawnParticle(Particle.WATER_SPLASH, loc, 100);
    	                
    	                double random = Math.random();
    	                if(d >= 1) {
    	                	if(random >= 0.2) {
    	                		if(world.getBlockAt(loc).isEmpty()) {
    	                			world.getBlockAt(loc).setType(Material.WATER);
    	                		}
    	                		else {
    	                			d = d + this.pl.getConfig().getInt("Equipment.Hose.range");
    	                		}
        	                }
    	                }
    	            }
    	            
    	            event.setCancelled(true);
            	}
    		}
    		
        	// pompe a eau
    		ItemStack pump = new ItemStack(Material.CLAY_BALL);
	        ItemMeta meta1 = pump.getItemMeta();
	       
	        meta1.addEnchant(Enchantment.WATER_WORKER, 10, true);
	        meta1.setDisplayName(ChatColor.BLUE + this.pl.getConfig().getString("Equipment.Pump.displayName").replace("&", "§"));
	        pump.setItemMeta(meta1);
	        
    		if(player.getInventory().getItemInMainHand().equals(pump)) {
    			if(player.hasPermission("firequip.tools.pump")) {
    	        	Location loc = player.getEyeLocation();
    	            World world = player.getWorld();
    	            
    	            for(double d = 0; d <= this.pl.getConfig().getInt("Equipment.Pump.range"); d += 0.75){
    	                loc.add(loc.getDirection());
    	                world.playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, (float) 0.5, -2);
    	                world.spawnParticle(Particle.TOWN_AURA, loc, 10);

                		if(world.getBlockAt(loc).isLiquid()) {
                			world.getBlockAt(loc).setType(Material.AIR);
                		}
    	            }
            	}
    		}
    		
        	// exctincteur
    		ItemStack extinguisher = new ItemStack(Material.IRON_HOE);
	        ItemMeta meta2 = extinguisher.getItemMeta();

	        meta2.addEnchant(Enchantment.PROTECTION_FIRE, 10, true);
	        meta2.setDisplayName(ChatColor.WHITE + this.pl.getConfig().getString("Equipment.Extinguisher.displayName").replace("&", "§"));
	        extinguisher.setItemMeta(meta2);
	        
    		if(player.getInventory().getItemInMainHand().equals(extinguisher)) {
    			if(player.hasPermission("firequip.tools.extinguisher")) {
    	        	Location loc = player.getEyeLocation();
    	            World world = player.getWorld();
    	            
    	            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 15, 1));
    	            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 15, 999));
    	            
    	            for(double d = 0; d <= this.pl.getConfig().getInt("Equipment.Hose.range"); d += 1){
    	                loc.add(loc.getDirection());
    	                world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 3, 10);
    	                world.spawnParticle(Particle.FALLING_DUST, loc, 10);
    	                
    	                double random = Math.random();
    	                if(d >= 1) {
    	                	if(random >= 0.5) {
    	                		if(world.getBlockAt(loc).getType().equals(Material.FIRE)) {
    	                			world.getBlockAt(loc).setType(Material.AIR);
    	                		}
        	                }
    	                }
    	            }
    	            
    	            event.setCancelled(true);
            	}
    		}
        	
	    }
    }
	
}

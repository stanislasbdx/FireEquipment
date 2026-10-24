package fr.stan1712.wetston.fireequipment.tools;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.fire.FireManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HoseTool implements FireTool {
	private final Plugin plugin;
	private final PluginSettings settings;
	private final FireManager fire;
	private final Map<Block, Integer> placedWater = new HashMap<>();
	private int waterToken;

	public HoseTool(Plugin plugin, PluginSettings settings, FireManager fire) {
		this.plugin = plugin;
		this.settings = settings;
		this.fire = fire;
	}

	@Override
	public ToolType type() {
		return ToolType.HOSE;
	}

	@Override
	public void use(Player player) {
		final Location loc = player.getEyeLocation();
		final World world = player.getWorld();

		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15, 10));

		for(int step = 0; step < settings.tool(ToolType.HOSE).range(); step++) {
			loc.add(loc.getDirection());
			world.playSound(loc, Sound.ENTITY_DOLPHIN_SPLASH, 5, 5);
			world.spawnParticle(Particle.SPLASH, loc, 20);
			world.spawnParticle(Particle.FALLING_WATER, loc, 10);

			final Block block = world.getBlockAt(loc);
			fire.applyWater(block);
			if(step < 1) continue;

			if(block.getType() == Material.WATER) {
				if(placedWater.containsKey(block)) scheduleWaterRemoval(block);
				continue;
			}
			if(fire.isShatterable(block)) {
				if(!Protection.canBreak(player, block)) break;
				fire.shatter(block);
				continue;
			}
			if(!block.isEmpty() && block.getType() != Material.FIRE) break;
			if(Math.random() < 0.2) continue;
			if(!Protection.canPlace(player, block)) break;

			block.setType(Material.WATER);
			scheduleWaterRemoval(block);
		}
	}

	@Override
	public void shutdown() {
		new ArrayList<>(placedWater.keySet()).forEach(block -> removeWater(block, placedWater.get(block)));
	}

	private void scheduleWaterRemoval(Block block) {
		final int token = ++waterToken;
		placedWater.put(block, token);
		Bukkit.getScheduler().runTaskLater(plugin, () -> removeWater(block, token), settings.hoseWaterLifetimeTicks());
	}

	private void removeWater(Block block, int token) {
		if(placedWater.remove(block, token) && block.getType() == Material.WATER) {
			block.setType(Material.AIR);
		}
	}
}

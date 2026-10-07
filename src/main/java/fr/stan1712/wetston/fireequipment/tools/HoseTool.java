package fr.stan1712.wetston.fireequipment.tools;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
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
import java.util.HashSet;
import java.util.Set;

public class HoseTool implements FireTool {
	private final Plugin plugin;
	private final PluginSettings settings;
	private final Set<Block> placedWater = new HashSet<>();

	public HoseTool(Plugin plugin, PluginSettings settings) {
		this.plugin = plugin;
		this.settings = settings;
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

		for(int step = 0; step <= settings.tool(ToolType.HOSE).range(); step++) {
			loc.add(loc.getDirection());
			world.playSound(loc, Sound.ENTITY_DOLPHIN_SPLASH, 5, 5);
			world.spawnParticle(Particle.SPLASH, loc, 20);
			world.spawnParticle(Particle.FALLING_WATER, loc, 10);

			if(step >= 1 && Math.random() >= 0.2) {
				final Block block = world.getBlockAt(loc);
				if(!block.isEmpty() && block.getType() != Material.FIRE) break;
				if(!Protection.canPlace(player, block)) break;

				block.setType(Material.WATER);
				scheduleWaterRemoval(block);
			}
		}
	}

	@Override
	public void shutdown() {
		new ArrayList<>(placedWater).forEach(this::removeWater);
	}

	private void scheduleWaterRemoval(Block block) {
		placedWater.add(block);
		Bukkit.getScheduler().runTaskLater(plugin, () -> removeWater(block), settings.hoseWaterLifetimeTicks());
	}

	private void removeWater(Block block) {
		if(placedWater.remove(block) && block.getType() == Material.WATER) {
			block.setType(Material.AIR);
		}
	}
}

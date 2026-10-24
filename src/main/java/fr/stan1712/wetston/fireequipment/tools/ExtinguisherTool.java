package fr.stan1712.wetston.fireequipment.tools;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.fire.FireManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ExtinguisherTool implements FireTool {
	private final PluginSettings settings;
	private final FireManager fire;

	public ExtinguisherTool(PluginSettings settings, FireManager fire) {
		this.settings = settings;
		this.fire = fire;
	}

	@Override
	public ToolType type() {
		return ToolType.EXTINGUISHER;
	}

	@Override
	public void use(Player player) {
		final Location loc = player.getEyeLocation();
		final World world = player.getWorld();

		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15, 1));

		for(int step = 0; step < settings.tool(ToolType.EXTINGUISHER).range(); step++) {
			loc.add(loc.getDirection());
			world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 3, 10);
			world.spawnParticle(Particle.FALLING_WATER, loc, 4);

			final Block block = world.getBlockAt(loc);
			if(fire.applyPowder(block)) {
				if(block.getType() == Material.FIRE && !fire.supportsFlame(block) && Protection.canBreak(player, block)) {
					block.setType(Material.AIR);
				}
				continue;
			}

			if(step >= 1 && Math.random() >= 0.5 && block.getType() == Material.FIRE && Protection.canBreak(player, block)) {
				block.setType(Material.AIR);
			}
		}
	}
}

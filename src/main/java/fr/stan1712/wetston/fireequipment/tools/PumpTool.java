package fr.stan1712.wetston.fireequipment.tools;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PumpTool implements FireTool {
	private static final double STEP = 0.75;

	private final PluginSettings settings;

	public PumpTool(PluginSettings settings) {
		this.settings = settings;
	}

	@Override
	public ToolType type() {
		return ToolType.PUMP;
	}

	@Override
	public void use(Player player) {
		final Location loc = player.getEyeLocation();
		final World world = player.getWorld();

		for(double d = 0; d <= settings.tool(ToolType.PUMP).range(); d += STEP) {
			loc.add(loc.getDirection());
			world.playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, 0.5f, -2);
			world.spawnParticle(Particle.ASH, loc, 10);

			final Block block = world.getBlockAt(loc);
			if(block.getType() == Material.WATER && Protection.canBreak(player, block)) {
				block.setType(Material.AIR);
			}
		}
	}
}

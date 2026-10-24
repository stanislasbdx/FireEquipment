package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.concurrent.ThreadLocalRandom;

public class WeakBlockBurster {
	private final FireManager fire;

	public WeakBlockBurster(FireManager fire) {
		this.fire = fire;
	}

	public void scanAround(Block center) {
		final FireSettings.WeakBlocks weakBlocks = fire.getSettings().weakBlocks();
		final boolean bursts = weakBlocks.enabled() && !weakBlocks.materials().isEmpty();
		final boolean tnt = fire.getSettings().features().tnt();
		if(!bursts && !tnt) {
			return;
		}

		final World world = center.getWorld();
		final int radius = weakBlocks.radius();
		final ThreadLocalRandom random = ThreadLocalRandom.current();

		for(int dx = -radius; dx <= radius; dx++) {
			for(int dy = -radius; dy <= radius; dy++) {
				for(int dz = -radius; dz <= radius; dz++) {
					final Block target = FireBlocks.blockAt(world, center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					if(target == null) {
						continue;
					}

					final boolean isTnt = tnt && target.getType() == Material.TNT;
					if(!isTnt && !(bursts && weakBlocks.materials().contains(target.getType()))) {
						continue;
					}

					final double heat = fire.heatAround(world.getUID(), target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, radius, true);
					if(isTnt) {
						if(heat >= FireDefaults.TNT_PRIME_HEAT && random.nextDouble() < FireDefaults.TNT_PRIME_CHANCE) {
							fire.primeTnt(target);
						}
					}
					else if(heat >= weakBlocks.burstHeat() && random.nextDouble() < weakBlocks.burstChance()) {
						burst(target, radius);
					}
				}
			}
		}
	}

	public boolean isShatterable(Block block) {
		final FireSettings.WeakBlocks weakBlocks = fire.getSettings().weakBlocks();
		return weakBlocks.enabled() && weakBlocks.materials().contains(block.getType()) && block.getType().name().contains("GLASS");
	}

	public void burst(Block block, int radius) {
		final BlockKey key = BlockKey.of(block);
		final BurningBlock burningBlock = fire.get(key);
		if(burningBlock != null) {
			fire.extinguish(burningBlock);
		}

		final BlockData data = block.getBlockData();
		final Location location = block.getLocation().add(0.5, 0.5, 0.5);
		final World world = block.getWorld();

		block.setType(Material.AIR);
		fire.getCollapse().collapseAround(block);
		fire.getBackdraft().onOpened(block);
		world.playSound(location, data.getSoundGroup().getBreakSound(), 1.2f, 0.8f);
		world.spawnParticle(Particle.BLOCK, location, 30, 0.3, 0.3, 0.3, 0, data);
		fire.invalidateEnvironment(key, radius + 2);
	}
}

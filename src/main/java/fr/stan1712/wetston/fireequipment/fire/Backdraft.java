package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Backdraft {
	private final Plugin plugin;
	private final FireManager fire;

	public Backdraft(Plugin plugin, FireManager fire) {
		this.plugin = plugin;
		this.fire = fire;
	}

	public record Pressure(Block room, double ratio, boolean confined) {
		public static final Pressure NONE = new Pressure(null, 0, false);
	}

	public void onOpened(Block opening) {
		final FireSettings settings = fire.getSettings();
		if(!settings.features().backdraft() || !fire.isWorldEnabled(opening.getWorld())) {
			return;
		}

		fire.invalidateEnvironment(BlockKey.of(opening), FireDefaults.BACKDRAFT_RADIUS * 2);

		final Pressure pressure = assess(opening);
		if(pressure.room() == null || pressure.ratio() < 1 || fire.isZoneCoolingDown(pressure.room())) {
			return;
		}

		final Block room = pressure.room();
		fire.claimZone(room);
		final Location center = opening.getLocation().add(0.5, 0.5, 0.5);
		final Vector outward = center.toVector().subtract(room.getLocation().add(0.5, 0.5, 0.5).toVector()).normalize();
		inhale(center, outward, pressure.confined() ? 0.5 : 0.25);

		Bukkit.getScheduler().runTaskLater(plugin, () -> explode(center, room, outward), FireDefaults.BACKDRAFT_DELAY_TICKS);
	}

	public Pressure assess(Block opening) {
		final FireSettings settings = fire.getSettings();
		if(!settings.features().backdraft() || !fire.isWorldEnabled(opening.getWorld())) {
			return Pressure.NONE;
		}

		final Set<Long> blocked = new HashSet<>();
		blocked.add(BlockKey.of(opening).pack());
		if(opening.getBlockData() instanceof Bisected bisected) {
			final Block other = FireBlocks.neighbor(opening, bisected.getHalf() == Bisected.Half.BOTTOM ? BlockFace.UP : BlockFace.DOWN);
			if(other != null) {
				blocked.add(BlockKey.of(other).pack());
			}
		}

		Pressure worst = Pressure.NONE;
		for(BlockFace face : FireBlocks.FACES) {
			final Block cell = FireBlocks.neighbor(opening, face);
			if(cell == null || !FireEnvironment.isTraversable(cell)) {
				continue;
			}

			final double heat = fire.heatAround(cell.getWorld().getUID(), cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5, FireDefaults.BACKDRAFT_RADIUS, false);
			if(heat <= 0) {
				continue;
			}

			final FireEnvironment.Environment environment = FireEnvironment.room(cell, blocked, settings.confinement().maxRoomVolume());
			final double share = environment.confined() ? FireDefaults.BACKDRAFT_HEAT_SHARE : FireDefaults.BACKDRAFT_VENTILATED_HEAT_SHARE;
			final double ratio = heat / (settings.confinement().flashoverHeat() * share);
			if(ratio > worst.ratio()) {
				worst = new Pressure(cell, ratio, environment.confined());
			}
		}

		return worst;
	}

	private void inhale(Location center, Vector outward, double pull) {
		final World world = center.getWorld();
		world.playSound(center, Sound.ENTITY_BREEZE_INHALE, 3f, 0.6f);
		world.spawnParticle(Particle.GUST, center, 3, 0.3, 0.5, 0.3, 0);

		for(int i = 1; i <= 4; i++) {
			final Location point = center.clone().add(outward.clone().multiply(i));
			final Vector inward = outward.clone().multiply(-0.25);
			for(int j = 0; j < 6; j++) {
				world.spawnParticle(Particle.CLOUD, point.clone().add(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5), 0, inward.getX(), inward.getY(), inward.getZ());
			}
		}

		for(Player player : world.getPlayers()) {
			if(isExposed(player, center, outward)) {
				player.setVelocity(player.getVelocity().add(outward.clone().multiply(-pull).setY(0.1)));
			}
		}
	}

	private void explode(Location center, Block room, Vector outward) {
		final World world = center.getWorld();
		if(!world.isChunkLoaded(center.getBlockX() >> 4, center.getBlockZ() >> 4)) {
			return;
		}

		fire.engulf(room, FireDefaults.BACKDRAFT_RADIUS);

		for(int i = 0; i <= FireDefaults.BACKDRAFT_REACH; i++) {
			final Location point = center.clone().add(outward.clone().multiply(i));
			final double spread = 0.4 + i * 0.25;
			world.spawnParticle(Particle.FLAME, point, 40, spread, spread, spread, 0.08);
			world.spawnParticle(Particle.LARGE_SMOKE, point, 10, spread, spread, spread, 0.05);

			final Block cell = FireBlocks.blockAt(world, point.getBlockX(), point.getBlockY(), point.getBlockZ());
			if(cell != null && i > 0 && Math.random() < 0.6) {
				fire.placeEphemeralFlame(cell);
			}
		}

		world.playSound(center, Sound.ENTITY_BREEZE_WIND_BURST, FireDefaults.FLASHOVER_VOLUME, 0.6f);
		world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, FireDefaults.FLASHOVER_VOLUME, 0.7f);

		for(Player player : world.getPlayers()) {
			if(isExposed(player, center, outward)) {
				player.setVelocity(outward.clone().multiply(1.2).setY(0.35));
				player.setFireTicks(Math.max(player.getFireTicks(), FireDefaults.BACKDRAFT_FIRE_TICKS));
			}
		}

		fire.invalidateEnvironment(BlockKey.of(room), FireDefaults.BACKDRAFT_RADIUS * 2);
	}

	private static boolean isExposed(Player player, Location center, Vector outward) {
		if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}

		final Vector toPlayer = player.getLocation().add(0, 1, 0).toVector().subtract(center.toVector());
		final double distance = toPlayer.length();
		return distance <= FireDefaults.BACKDRAFT_REACH && (distance < 1 || toPlayer.normalize().dot(outward) > 0.4);
	}
}

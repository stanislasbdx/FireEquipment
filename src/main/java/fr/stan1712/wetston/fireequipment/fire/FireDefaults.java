package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Material;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class FireDefaults {
	private static final double SHATTER_WEIGHT = 0.15;

	public record Crack(Material target, double weight) {}

	public static final Map<Material, Crack> CRACKS = crackTable();

	private static Map<Material, Crack> crackTable() {
		final Map<Material, Crack> cracks = new EnumMap<>(Material.class);
		cracks.put(Material.STONE_BRICKS, new Crack(Material.CRACKED_STONE_BRICKS, 1));
		cracks.put(Material.DEEPSLATE_BRICKS, new Crack(Material.CRACKED_DEEPSLATE_BRICKS, 1));
		cracks.put(Material.DEEPSLATE_TILES, new Crack(Material.CRACKED_DEEPSLATE_TILES, 1));
		cracks.put(Material.NETHER_BRICKS, new Crack(Material.CRACKED_NETHER_BRICKS, 1));
		cracks.put(Material.POLISHED_BLACKSTONE_BRICKS, new Crack(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, 1));
		cracks.put(Material.STONE, new Crack(Material.COBBLESTONE, SHATTER_WEIGHT));
		cracks.put(Material.DEEPSLATE, new Crack(Material.COBBLED_DEEPSLATE, SHATTER_WEIGHT));
		return Collections.unmodifiableMap(cracks);
	}

	private FireDefaults() {
		throw new IllegalStateException("Utility class");
	}

	public static final double AMBIENT_CELSIUS = 20;
	public static final double CELSIUS_PER_HEAT = 5;

	public static double celsius(double heat) {
		return AMBIENT_CELSIUS + heat * CELSIUS_PER_HEAT;
	}

	public static final int UPDATE_INTERVAL = 10;
	public static final int MAX_BURNING_BLOCKS = 3000;
	public static final int MAX_UPDATES_PER_CYCLE = 400;

	public static final double HEAT_IGNITION = 45;
	public static final double HEAT_GROWTH = 6;
	public static final double HEAT_DISSIPATION = 0.6;
	public static final double EXTINGUISH_THRESHOLD = 15;
	public static final double REIGNITE_THRESHOLD = 40;
	public static final double REIGNITE_DELAY_SECONDS = 10;

	public static final int SPREAD_RADIUS = 1;
	public static final double SPREAD_CHANCE = 0.08;
	public static final double HEAT_PER_NEIGHBOR = 0.15;
	public static final double UPWARD_BONUS = 2;

	public static final double OPEN_AIR_BURN_SPEED = 1.25;
	public static final double OPEN_AIR_DISSIPATION = 2;
	public static final double RAIN_COOLING = 2;

	public static final int MAX_ROOM_VOLUME = 150;
	public static final double CONFINED_BURN_SPEED = 0.7;
	public static final double CONFINED_HEAT_MULTIPLIER = 1.8;
	public static final double CONFINED_DISSIPATION = 0.25;
	public static final int FLASHOVER_RADIUS = 3;
	public static final double FLASHOVER_HEAT = 600;
	public static final double FLASHOVER_COOLDOWN_SECONDS = 30;
	public static final double FLASHOVER_IGNITE_CHANCE = 0.6;
	public static final double FLASHOVER_HEAT_RATIO = 0.5;
	public static final float FLASHOVER_VOLUME = 6;
	public static final double EPHEMERAL_FLAME_CHANCE = 0.25;
	public static final int BACKDRAFT_RADIUS = 4;
	public static final double BACKDRAFT_CONFINED_CELSIUS = 250;
	public static final double BACKDRAFT_VENTILATED_CELSIUS = 450;
	public static final int BACKDRAFT_SAMPLE_DEPTH = 2;
	public static final long BACKDRAFT_COOLDOWN_TICKS = 200;
	public static final long BACKDRAFT_DELAY_TICKS = 20;
	public static final int BACKDRAFT_REACH = 6;
	public static final int BACKDRAFT_FIRE_TICKS = 100;
	public static final long EPHEMERAL_FLAME_MIN_TICKS = 60;
	public static final long EPHEMERAL_FLAME_MAX_TICKS = 120;

	public static final double HOSE_COOLING = 12;
	public static final double POWDER_KNOCKDOWN = 90;
	public static final double POWDER_COOLING = 5;
	public static final double ADJACENT_WATER_COOLING = 10;

	public static final int AURA_RADIUS = 6;
	public static final double AURA_WARN_THRESHOLD = 60;
	public static final double AURA_IGNITE_THRESHOLD = 150;
	public static final double AURA_FIRE_TICKS_PER_HEAT = 2;
	public static final int AURA_MAX_FIRE_TICKS = 160;

	public static final int WEAK_RADIUS = 4;
	public static final double WEAK_BURST_HEAT = 120;
	public static final double WEAK_BURST_CHANCE = 0.25;

	public static final double AUTOIGNITION_HEAT = 60;
	public static final int AUTOIGNITION_RADIUS = 3;
	public static final double SMOULDERING_HEAT_SHARE = 0.5;
	public static final double HOT_SPOT_HEAT_SHARE = 0.5;
	public static final double HOT_SPOT_IGNITE_CHANCE = 0.2;

	public static final double SCORCH_HEAT_RATIO = 0.5;
	public static final double SCORCH_CHANCE_PER_SECOND = 0.2;
	public static final int SCORCH_RADIUS = 2;
	public static final int SCORCH_DEPTH = 2;

	public static final double CRACK_HEAT_RATIO = 0.8;
	public static final double CRACK_CHANCE_PER_SECOND = 0.01;
	public static final double CRACK_ENGULF_CHANCE = 0.15;
	public static final double CRACK_FALLING_CHANCE = 0.3;
	public static final double CRACK_COLLAPSE_CHANCE = 0.1;

	public static final int TNT_FUSE_TICKS = 80;
	public static final double TNT_PRIME_HEAT = 30;
	public static final double TNT_PRIME_CHANCE = 0.5;

	public static final double ASH_CHANCE = 0.35;
	public static final double ASH_PARTICLE_SECONDS = 60;
	public static final int MAX_PARTICLES_PER_CYCLE = 1200;
	public static final double INTENSE_FIRE_RATIO = 0.6;

	public static final int COLLAPSE_MAX_STRUCTURE = 128;
	public static final int COLLAPSE_EFFECT_THRESHOLD = 6;
	public static final double COLLAPSE_MAX_VOLUME = 8;
}

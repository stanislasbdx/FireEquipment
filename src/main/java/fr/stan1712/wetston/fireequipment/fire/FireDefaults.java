package fr.stan1712.wetston.fireequipment.fire;

public final class FireDefaults {
	private FireDefaults() {
		throw new IllegalStateException("Utility class");
	}

	public static final int UPDATE_INTERVAL = 10;
	public static final int MAX_BURNING_BLOCKS = 3000;
	public static final int MAX_UPDATES_PER_CYCLE = 400;

	public static final double HEAT_IGNITION = 45;
	public static final double HEAT_GROWTH = 6;
	public static final double HEAT_DISSIPATION = 1;
	public static final double EXTINGUISH_THRESHOLD = 15;
	public static final double REIGNITE_THRESHOLD = 40;
	public static final double REIGNITE_DELAY_SECONDS = 10;

	public static final int SPREAD_RADIUS = 1;
	public static final double SPREAD_CHANCE = 0.08;
	public static final double HEAT_PER_NEIGHBOR = 0.15;
	public static final double UPWARD_BONUS = 2;

	public static final double OPEN_AIR_BURN_SPEED = 1.25;
	public static final double OPEN_AIR_DISSIPATION = 3;
	public static final double RAIN_COOLING = 2;

	public static final int MAX_ROOM_VOLUME = 150;
	public static final double CONFINED_BURN_SPEED = 0.7;
	public static final double CONFINED_HEAT_MULTIPLIER = 1.8;
	public static final int FLASHOVER_RADIUS = 3;
	public static final double FLASHOVER_HEAT = 600;
	public static final double FLASHOVER_COOLDOWN_SECONDS = 30;
	public static final double FLASHOVER_IGNITE_CHANCE = 0.6;
	public static final double FLASHOVER_HEAT_RATIO = 0.5;

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

	public static final double ASH_CHANCE = 0.35;
	public static final double ASH_PARTICLE_SECONDS = 60;
	public static final int MAX_PARTICLES_PER_CYCLE = 300;

	public static final int COLLAPSE_MAX_STRUCTURE = 128;
	public static final int COLLAPSE_EFFECT_THRESHOLD = 6;
}

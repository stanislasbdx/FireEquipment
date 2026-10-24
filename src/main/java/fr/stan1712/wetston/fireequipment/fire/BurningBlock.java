package fr.stan1712.wetston.fireequipment.fire;

public class BurningBlock {
	private final BlockKey key;
	private final FireCategory category;
	private double remainingTicks;
	private double heat;
	private FireState state = FireState.BURNING;
	private long stateChangedAt;
	private long lastUpdate;
	private FireEnvironment.Environment environment;
	private long environmentAt;

	public BurningBlock(BlockKey key, FireCategory category, double remainingTicks, double heat) {
		this.key = key;
		this.category = category;
		this.remainingTicks = remainingTicks;
		this.heat = heat;
	}

	public BlockKey getKey() {
		return key;
	}

	public FireCategory getCategory() {
		return category;
	}

	public double getRemainingTicks() {
		return remainingTicks;
	}

	public void setRemainingTicks(double remainingTicks) {
		this.remainingTicks = remainingTicks;
	}

	public double getHeat() {
		return heat;
	}

	public void setHeat(double heat) {
		this.heat = heat;
	}

	public void setState(FireState state) {
		this.state = state;
	}

	public long getStateChangedAt() {
		return stateChangedAt;
	}

	public void setStateChangedAt(long stateChangedAt) {
		this.stateChangedAt = stateChangedAt;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public FireEnvironment.Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(FireEnvironment.Environment environment) {
		this.environment = environment;
	}

	public long getEnvironmentAt() {
		return environmentAt;
	}

	public void setEnvironmentAt(long environmentAt) {
		this.environmentAt = environmentAt;
	}

	public boolean isBurning() {
		return state == FireState.BURNING;
	}

	public double heatRatio() {
		return category.maxHeat() <= 0 ? 0 : heat / category.maxHeat();
	}
}

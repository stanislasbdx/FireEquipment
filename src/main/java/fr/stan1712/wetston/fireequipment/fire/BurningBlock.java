package fr.stan1712.wetston.fireequipment.fire;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BurningBlock {
	private final BlockKey key;
	private final FireCategory category;
	private double remainingTicks;
	private double heat;
	private FireState state = FireState.BURNING;
	private long stateChangedAt;
	private long lastUpdate;

	public BurningBlock(BlockKey key, FireCategory category, double remainingTicks, double heat) {
		this.key = key;
		this.category = category;
		this.remainingTicks = remainingTicks;
		this.heat = heat;
	}

	public boolean isBurning() {
		return state == FireState.BURNING;
	}

	public double heatRatio() {
		return category.maxHeat() <= 0 ? 0 : heat / category.maxHeat();
	}
}

package fr.stan1712.wetston.fireequipment.fire;

public record FireCategory(String name, int burnTicks, double ignitionChance, double maxHeat, boolean ash) {
}

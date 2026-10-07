package fr.stan1712.wetston.fireequipment.tools;

import org.bukkit.entity.Player;

public interface FireTool {
	ToolType type();

	void use(Player player);

	default void shutdown() {
	}
}

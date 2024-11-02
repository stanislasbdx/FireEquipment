package fr.stan1712.wetston.fireequipment.defaults;

import org.bukkit.ChatColor;

public class StrStructure {
	private StrStructure() {
		throw new IllegalStateException("Utility class");
	}

	public static final String BOTTOM_BOX = ChatColor.RED + "+----- ----- ----- -----+";

	public static final String START_TITLE_BOX = ChatColor.RED + "+----- ▲ ";
	public static final String END_TITLE_BOX = ChatColor.RED + " ▲ -----+";
}

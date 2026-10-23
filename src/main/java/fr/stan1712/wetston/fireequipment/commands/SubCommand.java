package fr.stan1712.wetston.fireequipment.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

interface SubCommand {
	String name();

	default String permission() {
		return "firequip.info";
	}

	void execute(CommandSender sender, String[] args);

	default List<String> complete(CommandSender sender, String[] args) {
		return List.of();
	}
}

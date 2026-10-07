package fr.stan1712.wetston.fireequipment.messages;

import org.bukkit.command.CommandSender;

interface MessageSender {
	void send(CommandSender to, String text);
}

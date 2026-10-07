package fr.stan1712.wetston.fireequipment.messages;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

final class LegacyMessageSender implements MessageSender {
	@Override
	public void send(CommandSender to, String text) {
		to.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
	}
}

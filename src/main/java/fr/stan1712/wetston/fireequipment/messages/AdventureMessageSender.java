package fr.stan1712.wetston.fireequipment.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

final class AdventureMessageSender implements MessageSender {
	private final MiniMessage miniMessage = MiniMessage.miniMessage();
	private final MessageSender fallback = new LegacyMessageSender();

	@Override
	public void send(CommandSender to, String text) {
		if(to instanceof Audience audience) {
			audience.sendMessage(miniMessage.deserialize(LegacyToMiniMessage.convert(text)));
		} else {
			fallback.send(to, text);
		}
	}
}

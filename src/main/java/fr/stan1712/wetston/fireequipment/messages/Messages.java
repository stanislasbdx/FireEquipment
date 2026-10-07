package fr.stan1712.wetston.fireequipment.messages;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class Messages {
	private static final String ADVENTURE_CLASS = "net.kyori.adventure.text.minimessage.MiniMessage";
	private static final String BORDER_COLOR = "&c";

	private final Plugin plugin;
	private final PluginSettings settings;
	private final MessageSender sender;
	private final MessageSender fallback = new LegacyMessageSender();

	public Messages(Plugin plugin, PluginSettings settings) {
		this.plugin = plugin;
		this.settings = settings;
		this.sender = createSender();
	}

	public static String legacy(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	private MessageSender createSender() {
		try {
			Class.forName(ADVENTURE_CLASS);
			return new AdventureMessageSender();
		} catch (ClassNotFoundException | LinkageError e) {
			return new LegacyMessageSender();
		}
	}

	public String get(String path) {
		final String value = plugin.getConfig().getString(path);

		return value == null ? path : value;
	}

	public void send(CommandSender to, String text) {
		try {
			sender.send(to, text);
		} catch (LinkageError e) {
			fallback.send(to, text);
		}
	}

	public void sendKey(CommandSender to, String path) {
		send(to, get(path));
	}

	public void noPermission(CommandSender to) {
		send(to, "[" + settings.prefix() + "] " + get("Core.NoPerms"));
	}

	public void box(CommandSender to, List<String> lines) {
		send(to, BORDER_COLOR + "+----- ▲ " + settings.prefix() + BORDER_COLOR + " ▲ -----+");
		lines.forEach(line -> send(to, "&f» " + line));
		send(to, BORDER_COLOR + "+----- ----- ----- -----+");
	}

	public void box(CommandSender to, String... lines) {
		box(to, List.of(lines));
	}
}

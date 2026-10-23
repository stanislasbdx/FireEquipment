package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.messages.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

class VersionSubCommand implements SubCommand {
	private final Plugin plugin;
	private final Messages messages;

	VersionSubCommand(Plugin plugin, Messages messages) {
		this.plugin = plugin;
		this.messages = messages;
	}

	@Override
	public String name() {
		return "version";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		messages.box(sender, "Version " + plugin.getDescription().getVersion());
	}
}

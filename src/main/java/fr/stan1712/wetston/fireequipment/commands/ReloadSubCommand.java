package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import org.bukkit.command.CommandSender;

class ReloadSubCommand implements SubCommand {
	private final PluginSettings settings;
	private final Messages messages;

	ReloadSubCommand(PluginSettings settings, Messages messages) {
		this.settings = settings;
		this.messages = messages;
	}

	@Override
	public String name() {
		return "reload";
	}

	@Override
	public String permission() {
		return "firequip.admin.reload";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		settings.reload();
		messages.box(sender, messages.get("Core.Reload"));
	}
}

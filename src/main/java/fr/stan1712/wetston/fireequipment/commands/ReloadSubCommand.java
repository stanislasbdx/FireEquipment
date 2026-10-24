package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.fire.FireManager;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import org.bukkit.command.CommandSender;

class ReloadSubCommand implements SubCommand {
	private final PluginSettings settings;
	private final Messages messages;
	private final FireManager fire;

	ReloadSubCommand(PluginSettings settings, Messages messages, FireManager fire) {
		this.settings = settings;
		this.messages = messages;
		this.fire = fire;
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
		fire.reload();
		messages.box(sender, messages.get("Core.Reload"));
	}
}

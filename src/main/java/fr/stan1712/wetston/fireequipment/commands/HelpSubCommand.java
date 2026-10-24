package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.messages.Messages;
import org.bukkit.command.CommandSender;

class HelpSubCommand implements SubCommand {
	private final Messages messages;

	HelpSubCommand(Messages messages) {
		this.messages = messages;
	}

	@Override
	public String name() {
		return "help";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		messages.box(sender,
			"/fequip help = " + messages.get("Core.HelpMsg.DHelp"),
			"/fequip version = " + messages.get("Core.HelpMsg.DVersion"),
			"/fequip reload = " + messages.get("Core.HelpMsg.DReload"),
			"/fequip fire <stats/clear [radius]/inspect> = " + messages.get("Core.HelpMsg.DFire"),
			"/fequip <item> = " + messages.get("Core.HelpMsg.DGive")
		);
	}
}

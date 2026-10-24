package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.fire.BlockKey;
import fr.stan1712.wetston.fireequipment.fire.FireInspector;
import fr.stan1712.wetston.fireequipment.fire.FireManager;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

class FireSubCommand implements SubCommand {
	private static final String ADMIN_PERMISSION = "firequip.admin.fire";
	private static final String INSPECT = "inspect";

	private final FireManager fire;
	private final Messages messages;
	private final FireInspector inspector;

	FireSubCommand(FireManager fire, Messages messages, FireInspector inspector) {
		this.fire = fire;
		this.messages = messages;
		this.inspector = inspector;
	}

	@Override
	public String name() {
		return "fire";
	}

	@Override
	public String permission() {
		return "firequip.inspect";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length >= 2 && args[1].equalsIgnoreCase(INSPECT)) {
			inspect(sender);
		} else if(!sender.hasPermission(ADMIN_PERMISSION)) {
			messages.noPermission(sender);
		} else if(args.length >= 2 && args[1].equalsIgnoreCase("clear")) {
			clear(sender, args);
		} else {
			stats(sender);
		}
	}

	@Override
	public List<String> complete(CommandSender sender, String[] args) {
		if(args.length != 2) return List.of();

		final String typed = args[1].toLowerCase(Locale.ROOT);
		final List<String> actions = sender.hasPermission(ADMIN_PERMISSION) ? List.of("stats", "clear", INSPECT) : List.of(INSPECT);
		return actions.stream().filter(action -> action.startsWith(typed)).toList();
	}

	private void inspect(CommandSender sender) {
		if(!(sender instanceof Player player)) {
			messages.box(sender, messages.get("Core.FireMsg.PlayersOnly"));
			return;
		}

		final boolean enabled = inspector.toggle(player);
		messages.box(sender, messages.get(enabled ? "Core.FireMsg.Inspect.On" : "Core.FireMsg.Inspect.Off"));
	}

	private void clear(CommandSender sender, String[] args) {
		int cleared;

		if(args.length >= 3 && sender instanceof Player player) {
			try {
				cleared = fire.clear(BlockKey.of(player.getLocation().getBlock()), Integer.parseInt(args[2]));
			} catch (NumberFormatException e) {
				messages.box(sender, "/firequip fire clear [radius]");
				return;
			}
		} else {
			cleared = fire.clear(null, 0);
		}

		messages.box(sender, cleared + " " + messages.get("Core.FireMsg.Cleared"));
	}

	private void stats(CommandSender sender) {
		final long smouldering = fire.all().stream().filter(burningBlock -> !burningBlock.isBurning()).count();

		messages.box(sender,
			messages.get("Core.FireMsg.Status") + " : " + (fire.getSettings().enabled() ? "&aON" : "&cOFF"),
			messages.get("Core.FireMsg.Worlds") + " : " + String.join(", ", fire.getSettings().worlds()),
			messages.get("Core.FireMsg.Burning") + " : " + (fire.all().size() - smouldering),
			messages.get("Core.FireMsg.Smouldering") + " : " + smouldering
		);
	}
}

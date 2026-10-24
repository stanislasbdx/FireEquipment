package fr.stan1712.wetston.fireequipment.fire;

import fr.stan1712.wetston.fireequipment.messages.Messages;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class FireInspector {
	private static final String KEYS = "Core.FireMsg.Inspect.";
	private static final int TARGET_RANGE = 8;
	private static final int AREA_RADIUS = 10;

	private final Plugin plugin;
	private final FireManager fire;
	private final Messages messages;
	private final Set<UUID> inspecting = new HashSet<>();
	private BukkitTask task;

	public FireInspector(Plugin plugin, FireManager fire, Messages messages) {
		this.plugin = plugin;
		this.fire = fire;
		this.messages = messages;
	}

	public boolean toggle(Player player) {
		if(inspecting.remove(player.getUniqueId())) {
			if(inspecting.isEmpty() && task != null) {
				task.cancel();
				task = null;
			}
			return false;
		}

		inspecting.add(player.getUniqueId());
		if(task == null) {
			task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1, 10);
		}
		return true;
	}

	private void tick() {
		final Iterator<UUID> iterator = inspecting.iterator();

		while(iterator.hasNext()) {
			final Player player = Bukkit.getPlayer(iterator.next());
			if(player == null) {
				iterator.remove();
				continue;
			}
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(Messages.legacy(describe(player))));
		}

		if(inspecting.isEmpty() && task != null) {
			task.cancel();
			task = null;
		}
	}

	private String describe(Player player) {
		if(!fire.isWorldEnabled(player.getWorld())) {
			return "&7" + text("Disabled");
		}

		final FireSettings.HeatAura aura = fire.getSettings().heatAura();
		final Location location = player.getLocation().add(0, 1, 0);
		final UUID world = player.getWorld().getUID();
		final double heat = fire.heatAround(world, location.getX(), location.getY(), location.getZ(), aura.radius(), true);
		final String heatColor = heat >= aura.igniteThreshold() ? "&c" : heat >= aura.warnThreshold() ? "&6" : "&a";

		int burning = 0;
		int smouldering = 0;
		for(BurningBlock burningBlock : fire.burningWithin(world, location.getX(), location.getY(), location.getZ(), AREA_RADIUS)) {
			if(burningBlock.isBurning()) {
				burning++;
			} else {
				smouldering++;
			}
		}

		return heatColor + text("Heat") + " " + Math.round(heat)
				+ " &8| " + target(player)
				+ " &8| &c" + text("Burning") + " " + burning + " &6" + text("Smouldering") + " " + smouldering;
	}

	private String target(Player player) {
		Block block = player.getTargetBlockExact(TARGET_RANGE);
		if(block == null) {
			return "&7-";
		}
		if(block.getType() == Material.FIRE) {
			block = burningSupport(block);
			if(block == null) {
				return "&7" + text("Inert") + " (fire)";
			}
		}

		final BurningBlock burningBlock = fire.get(BlockKey.of(block));
		if(burningBlock != null) {
			final FireEnvironment.Environment environment = FireEnvironment.compute(block, fire.getSettings().confinement().maxRoomVolume());
			return (burningBlock.isBurning() ? "&c" + text("Burning") : "&6" + text("Smouldering"))
					+ " &f" + burningBlock.getCategory().name()
					+ " " + Math.round(burningBlock.getHeat()) + "/" + Math.round(burningBlock.getCategory().maxHeat())
					+ " " + Math.max(0, Math.round(burningBlock.getRemainingTicks() / 20)) + "s"
					+ " &7" + environment(environment);
		}

		final FireCategory category = fire.getSettings().categoryOf(block.getType());
		final String name = block.getType().name().toLowerCase(Locale.ROOT);
		if(category == null) {
			return "&7" + text("Inert") + " (" + name + ")";
		}

		final double heat = fire.heatAround(block.getWorld().getUID(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, fire.getSettings().heatAura().radius(), true);
		return "&e" + text("Flammable") + " &f" + category.name() + " (" + name + ") &7" + Math.round(heat);
	}

	private Block burningSupport(Block fireBlock) {
		for(BlockFace face : FireBlocks.FACES) {
			final Block neighbor = FireBlocks.neighbor(fireBlock, face);
			if(neighbor != null && fire.get(BlockKey.of(neighbor)) != null) {
				return neighbor;
			}
		}
		return null;
	}

	private String environment(FireEnvironment.Environment environment) {
		if(environment.openAir()) {
			return text("OpenAir");
		}
		return text(environment.confined() ? "Confined" : "Ventilated") + " " + environment.roomVolume();
	}

	private String text(String key) {
		return messages.get(KEYS + key);
	}
}

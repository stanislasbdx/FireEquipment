package fr.stan1712.wetston.fireequipment.fire;

import fr.stan1712.wetston.fireequipment.messages.Messages;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FireInspector {
	private static final String KEYS = "Core.FireMsg.Inspect.";
	private static final int TARGET_RANGE = 8;
	private static final double COLD_CELSIUS = 60;
	private static final double GAUGE_OVERSHOOT = 1.3;

	private final Plugin plugin;
	private final FireManager fire;
	private final Messages messages;
	private final Map<UUID, BossBar> inspecting = new HashMap<>();
	private BukkitTask task;

	public FireInspector(Plugin plugin, FireManager fire, Messages messages) {
		this.plugin = plugin;
		this.fire = fire;
		this.messages = messages;
		fire.addShutdownHook(this::shutdown);
	}

	public boolean toggle(Player player) {
		final BossBar existing = inspecting.remove(player.getUniqueId());
		if(existing != null) {
			existing.removeAll();
			stopIfIdle();
			return false;
		}

		final BossBar bar = Bukkit.createBossBar(text("Camera"), BarColor.BLUE, BarStyle.SEGMENTED_10);
		bar.addPlayer(player);
		inspecting.put(player.getUniqueId(), bar);
		if(task == null) {
			task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1, 10);
		}
		return true;
	}

	public void shutdown() {
		inspecting.values().forEach(BossBar::removeAll);
		inspecting.clear();
		stopIfIdle();
	}

	private void stopIfIdle() {
		if(inspecting.isEmpty() && task != null) {
			task.cancel();
			task = null;
		}
	}

	private void tick() {
		for(UUID uuid : new ArrayList<>(inspecting.keySet())) {
			final Player player = Bukkit.getPlayer(uuid);
			final BossBar bar = inspecting.get(uuid);

			if(player == null) {
				bar.removeAll();
				inspecting.remove(uuid);
				continue;
			}

			if(!fire.isWorldEnabled(player.getWorld())) {
				bar.setTitle(Messages.legacy("&7" + text("Disabled")));
				bar.setColor(BarColor.WHITE);
				bar.setProgress(0);
				actionBar(player, "");
				continue;
			}

			updateBar(player, bar);
			actionBar(player, target(player));
		}

		stopIfIdle();
	}

	private void updateBar(Player player, BossBar bar) {
		final FireSettings.HeatAura aura = fire.getSettings().heatAura();
		final Location location = player.getLocation().add(0, 1, 0);
		final double heat = fire.heatAround(player.getWorld().getUID(), location.getX(), location.getY(), location.getZ(), aura.radius(), true);
		final double celsius = FireDefaults.celsius(heat);
		final double max = FireDefaults.celsius(aura.igniteThreshold() * GAUGE_OVERSHOOT);

		bar.setTitle(Messages.legacy(text("Camera") + " &8· " + color(celsius) + Math.round(celsius) + " °C"));
		bar.setProgress(Math.max(0, Math.min(1, (celsius - FireDefaults.AMBIENT_CELSIUS) / (max - FireDefaults.AMBIENT_CELSIUS))));
		bar.setColor(barColor(celsius));
	}

	private BarColor barColor(double celsius) {
		final FireSettings.HeatAura aura = fire.getSettings().heatAura();
		final double warn = FireDefaults.celsius(aura.warnThreshold());
		final double ignite = FireDefaults.celsius(aura.igniteThreshold());

		if(celsius < COLD_CELSIUS) return BarColor.BLUE;
		if(celsius < warn) return BarColor.GREEN;
		if(celsius < (warn + ignite) / 2) return BarColor.YELLOW;
		return BarColor.RED;
	}

	private String color(double celsius) {
		return switch(barColor(celsius)) {
			case BLUE -> "&b";
			case GREEN -> "&a";
			case YELLOW -> "&e";
			default -> "&c";
		};
	}

	private void actionBar(Player player, String text) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(Messages.legacy(text)));
	}

	private String target(Player player) {
		Block block = player.getTargetBlockExact(TARGET_RANGE);
		if(block == null) {
			return "";
		}
		if(block.getType() == Material.FIRE) {
			final Block support = burningSupport(block);
			block = support == null ? block : support;
		}

		if(isOpening(block)) {
			return opening(block);
		}

		final BurningBlock burningBlock = fire.get(BlockKey.of(block));
		final FireCategory category = burningBlock != null ? burningBlock.getCategory() : fire.getSettings().categoryOf(block.getType());
		final double heat = burningBlock != null
				? burningBlock.getHeat()
				: fire.heatAround(block.getWorld().getUID(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, fire.getSettings().heatAura().radius(), true);
		final double celsius = FireDefaults.celsius(heat);
		final String label = category == null ? "&7" + text("Inert") : "&f" + categoryName(category);

		return label + " &8· " + color(celsius) + Math.round(celsius) + " °C &8· &7" + environment(block);
	}

	private static boolean isOpening(Block block) {
		final BlockData data = block.getBlockData();
		return (data instanceof Door || data instanceof TrapDoor || data instanceof Gate) && !((Openable) data).isOpen();
	}

	private String opening(Block block) {
		final Backdraft.Pressure pressure = fire.getBackdraft().assess(block);
		final String level;
		if(pressure.ratio() >= 1) {
			level = "&c" + text("Pressure.Critical");
		} else if(pressure.ratio() >= 0.5) {
			level = "&e" + text("Pressure.Moderate");
		} else {
			level = "&a" + text("Pressure.Low");
		}

		final double heat = fire.heatAround(block.getWorld().getUID(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5, fire.getSettings().heatAura().radius(), true);
		final double celsius = FireDefaults.celsius(heat);
		return "&f" + text("Pressure.Opening") + " &8· &7" + text("Pressure.Label") + " : " + level + " &8· " + color(celsius) + Math.round(celsius) + " °C";
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

	private String environment(Block block) {
		final FireEnvironment.Environment environment = FireEnvironment.compute(block, fire.getSettings().confinement().maxRoomVolume());
		if(environment.openAir()) {
			return text("OpenAir");
		}
		return text(environment.confined() ? "Confined" : "Ventilated");
	}

	private String categoryName(FireCategory category) {
		final String path = KEYS + "Categories." + category.name();
		final String value = messages.get(path);
		if(!value.equals(path)) {
			return value;
		}
		return Character.toUpperCase(category.name().charAt(0)) + category.name().substring(1);
	}

	private String text(String key) {
		return messages.get(KEYS + key);
	}
}

package fr.stan1712.wetston.fireequipment;

import fr.stan1712.wetston.fireequipment.commands.FireEquipmentCommand;
import fr.stan1712.wetston.fireequipment.commands.GiveCommand;
import fr.stan1712.wetston.fireequipment.config.ConfigMigrator;
import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import fr.stan1712.wetston.fireequipment.tools.ExtinguisherTool;
import fr.stan1712.wetston.fireequipment.tools.FireTool;
import fr.stan1712.wetston.fireequipment.tools.HoseTool;
import fr.stan1712.wetston.fireequipment.tools.ItemFactory;
import fr.stan1712.wetston.fireequipment.tools.PumpTool;
import fr.stan1712.wetston.fireequipment.tools.ToolListener;
import fr.stan1712.wetston.fireequipment.utils.UpdateChecker;
import fr.stan1712.wetston.fireequipment.utils.Versions;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public final class Main extends JavaPlugin {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Core");

	public static final int SPIGOT_PLUGIN_ID = 69199;

	private PluginSettings settings;
	private Messages messages;
	private ItemFactory items;
	private ToolListener toolListener;

	public boolean versionCheck() {
		final String logStep = "versionCheck";
		final String serverVersion = getServer().getVersion();
		final String serverType = getServer().getName();
		final String minecraftVersion = Bukkit.getBukkitVersion();

		_log.info("[{}] Checking server version : {} {} (API {})", logStep, serverType, serverVersion, minecraftVersion);

		if(!Versions.isServerTypeSupported(serverType, serverVersion)) {
			_log.error("[{}] * Server type {} unknown, disabling plugin.", logStep, serverType);

			getServer().getPluginManager().disablePlugin(this);
			return false;
		}

		if(Versions.gameSupport(minecraftVersion) != Versions.Support.SUPPORTED) {
			_log.error("[{}] * Version {} is not supported by {}, disabling plugin.", logStep, minecraftVersion, getName());

			getServer().getPluginManager().disablePlugin(this);
			return false;
		}

		_log.info("[{}] Version check !", logStep);
		_log.info("[{}] If you got issues, report them on Github", logStep);
		return true;
	}

	private void updateCheck() {
		final String logStep = "updateCheck";
		new UpdateChecker(this, SPIGOT_PLUGIN_ID).getVersion(version -> {
			if(Versions.compare(version, this.getDescription().getVersion()) > 0) {
				_log.info("[{}] An update is available on Spigot ! ({})", logStep, version);
			}
		});
	}

	private void loadMetrics() {
		final String logStep = "loadMetrics";

		new Metrics(this, 23787);

		_log.info("{} bStats metrics loaded", logStep);
	}

	private void loadConfig() {
		ConfigMigrator.migrate(this);

		settings = new PluginSettings(this);
		messages = new Messages(this, settings);
		items = new ItemFactory(this, settings);
	}

	private void loadCommand(String logStep, String commandName, TabExecutor commandClass) {
		try {
			Objects.requireNonNull(getCommand(commandName)).setExecutor(commandClass);
			_log.info("[{}] /{} commands loaded", logStep, commandName);
		}
		catch (Exception e) {
			_log.error("Unable to load command /{} : {}", commandName, e, e);
		}
	}

	private void loadCommands() {
		final String logStep = "loadCommands";

		loadCommand(logStep, "firequip", new FireEquipmentCommand(this, settings, messages));
		loadCommand(logStep, "fequip", new GiveCommand(items, settings, messages));

		_log.info("[{}] Commands have been loaded !", logStep);
	}

	private void loadEvents() {
		final String logStep = "loadEvents";

		final List<FireTool> tools = List.of(new HoseTool(this, settings), new PumpTool(settings), new ExtinguisherTool(settings));
		toolListener = new ToolListener(items, settings, messages, tools);
		getServer().getPluginManager().registerEvents(toolListener, this);

		_log.info("[{}] {} tools loaded", logStep, tools.size());
	}

	private void logNewStep(String step) {
		_log.info("--- {} ---", step);
	}

	@Override
	public void onEnable() {
		logNewStep("updateCheck");
		updateCheck();

		logNewStep("versionCheck");
		final boolean versionCorrect = versionCheck();

		if(versionCorrect) {
			logNewStep("loadMetrics");
			loadMetrics();

			logNewStep("loadConfig");
			loadConfig();

			logNewStep("loadCommands");
			loadCommands();

			logNewStep("loadEvents");
			loadEvents();
		}
	}

	@Override
	public void onDisable() {
		if(toolListener != null) toolListener.shutdown();
	}
}

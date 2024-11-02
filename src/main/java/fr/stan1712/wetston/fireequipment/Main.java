package fr.stan1712.wetston.fireequipment;

import fr.stan1712.wetston.fireequipment.commands.FireEquipment;
import fr.stan1712.wetston.fireequipment.commands.GiveItem;
import fr.stan1712.wetston.fireequipment.events.Extinguisher;
import fr.stan1712.wetston.fireequipment.events.Hose;
import fr.stan1712.wetston.fireequipment.events.Pump;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JavaPlugin {
	private static final Logger _log = LoggerFactory.getLogger("FireEquipment - Core");
	public final PluginManager pluginManager = getServer().getPluginManager();

	public static final int SPIGOT_PLUGIN_ID = 69199;

	public boolean versionCheck() {
		final String logStep = "versionCheck";
		final String serverVersion = getServer().getVersion();
		final String serverType = getServer().getName();

		_log.info("[{}] Checking server version : {} {}", logStep, serverType, serverVersion);

		final Pattern versionPattern = Pattern.compile("\\d[.]\\d+", Pattern.MULTILINE);
		final Matcher versionMatcher = versionPattern.matcher(serverVersion);
		if(!versionMatcher.find() || (!serverType.contains("Spigot") && !serverType.contains("Paper"))) {
			_log.error("[{}] * Server type {} unknown, disabling plugin.", logStep, serverVersion);

			pluginManager.disablePlugin(this);
			return false;
		}
		final String version = versionMatcher.group();

		switch (version) {
			case "1.21", "1.20" -> {
				_log.info("[{}] Version check !", logStep);
				_log.info("[{}] If you got issues, report them on Github", logStep);
			}
			case "1.19", "1.18" -> {
				_log.info("[{}] {} may have issues while running !", logStep, version);
				_log.info("[{}] If you got any, report them on Github", logStep);
			}
			default -> {
				_log.error("[{}] * Version {} is not supported by {}, disabling plugin.", logStep, serverVersion, getName());

				pluginManager.disablePlugin(this);
				return false;
			}
		}

		return true;
	}

	private void updateCheck() {
		final String logStep = "updateCheck";
		new UpdateChecker(this, SPIGOT_PLUGIN_ID).getVersion(version -> {
			if(!this.getDescription().getVersion().equalsIgnoreCase(version)) {
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
		pluginManager.registerEvents(new Config(), this);
		saveConfig();
	}

	private void loadCommand(String logStep, String commandName, CommandExecutor commandClass) {
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

		loadCommand(logStep, "firequip", new FireEquipment(this));
		loadCommand(logStep, "fequip", new GiveItem(this));

		_log.info("[{}] Commands have been loaded !", logStep);
	}

	private void loadEvents() {
		final String logStep = "loadEvents";

		pluginManager.registerEvents(new Hose(this), this);
		_log.info("[{}] Hose event loaded", logStep);
		pluginManager.registerEvents(new Pump(this), this);
		_log.info("[{}] Pump event loaded", logStep);
		pluginManager.registerEvents(new Extinguisher(this), this);
		_log.info("[{}] Extinguisher event loaded", logStep);
	}

	private void logNewStep(String step) {
		_log.info("{@ {}} ---", step);
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
}

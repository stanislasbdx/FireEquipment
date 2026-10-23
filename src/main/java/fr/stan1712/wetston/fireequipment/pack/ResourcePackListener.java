package fr.stan1712.wetston.fireequipment.pack;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.config.ResourcePackSettings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ResourcePackListener implements Listener {
	private final PluginSettings settings;

	public ResourcePackListener(PluginSettings settings) {
		this.settings = settings;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		final ResourcePackSettings pack = settings.resourcePack();
		if(!pack.active()) return;

		event.getPlayer().setResourcePack(pack.url(), pack.sha1(), pack.prompt(), pack.force());
	}
}

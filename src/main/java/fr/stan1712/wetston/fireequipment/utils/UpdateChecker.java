package fr.stan1712.wetston.fireequipment.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

public class UpdateChecker {
	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private final Plugin plugin;
	private final int resourceId;

	public UpdateChecker(Plugin plugin, int resourceId) {
		this.plugin = plugin;
		this.resourceId = resourceId;
	}

	public void getVersion(final Consumer<String> consumer) {
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
			final HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId))
				.timeout(TIMEOUT)
				.GET()
				.build();

			try {
				final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
				final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				final String version = response.body().trim();

				if(response.statusCode() == 200 && !version.isEmpty()) {
					consumer.accept(version);
				}
			} catch (IOException exception) {
				this.plugin.getLogger().info("Cannot look for updates: " + exception.getMessage());
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
			}
		});
	}
}

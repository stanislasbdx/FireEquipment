package fr.stan1712.wetston.fireequipment.messages;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.bukkit.plugin.Plugin;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {
	private ServerMock server;
	private Messages messages;

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		final Plugin plugin = MockBukkit.createMockPlugin();
		messages = new Messages(plugin, new PluginSettings(plugin));
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void convertsLegacyCodesToMiniMessage() {
		assertEquals("<reset><red>Hello <bold>x<reset>", LegacyToMiniMessage.convert("&cHello &lx&r"));
		assertEquals("a & b", LegacyToMiniMessage.convert("a & b"));
		assertEquals("trailing &", LegacyToMiniMessage.convert("trailing &"));
	}

	@Test
	void sendsPlainTextThroughAdventure() {
		final PlayerMock player = server.addPlayer();

		messages.send(player, "&6Hello &f/fequip <item> = give");

		assertEquals("Hello /fequip <item> = give", PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage()));
	}

	@Test
	void missingKeyReturnsThePath() {
		assertEquals("Core.Nope", messages.get("Core.Nope"));
	}

	@Test
	void boxWrapsLinesWithTitleAndBottom() {
		final PlayerMock player = server.addPlayer();

		messages.box(player, "line");

		assertEquals("+----- ▲ FireEquipment ▲ -----+", PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage()));
		assertEquals("» line", PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage()));
		assertEquals("+----- ----- ----- -----+", PlainTextComponentSerializer.plainText().serialize(player.nextComponentMessage()));
	}
}

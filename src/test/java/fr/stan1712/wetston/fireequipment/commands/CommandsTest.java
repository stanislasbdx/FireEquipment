package fr.stan1712.wetston.fireequipment.commands;

import fr.stan1712.wetston.fireequipment.config.PluginSettings;
import fr.stan1712.wetston.fireequipment.messages.Messages;
import fr.stan1712.wetston.fireequipment.tools.ItemFactory;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandsTest {
	private ServerMock server;
	private FireEquipmentCommand fireEquipment;
	private GiveCommand give;

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		final Plugin plugin = MockBukkit.createMockPlugin();
		final PluginSettings settings = new PluginSettings(plugin);
		final Messages messages = new Messages(plugin, settings);

		fireEquipment = new FireEquipmentCommand(plugin, settings, messages);
		give = new GiveCommand(new ItemFactory(plugin, settings), settings, messages);
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void completesSubCommandsForOperators() {
		final PlayerMock op = server.addPlayer();
		op.setOp(true);

		assertEquals(List.of("help", "version", "reload"), fireEquipment.onTabComplete(op, null, "firequip", new String[]{""}));
		assertEquals(List.of("reload"), fireEquipment.onTabComplete(op, null, "firequip", new String[]{"R"}));
		assertEquals(List.of(), fireEquipment.onTabComplete(op, null, "firequip", new String[]{"reload", ""}));
	}

	@Test
	void completesNothingWithoutPermission() {
		final PlayerMock player = server.addPlayer();

		assertEquals(List.of(), fireEquipment.onTabComplete(player, null, "firequip", new String[]{""}));
		assertEquals(List.of(), give.onTabComplete(player, null, "fequip", new String[]{""}));
	}

	@Test
	void completesToolNames() {
		final PlayerMock op = server.addPlayer();
		op.setOp(true);

		assertEquals(List.of("hose", "pump", "extinguisher"), give.onTabComplete(op, null, "fequip", new String[]{""}));
		assertEquals(List.of("extinguisher"), give.onTabComplete(op, null, "fequip", new String[]{"e"}));
	}

	@Test
	void giveAddsTheToolToTheInventory() {
		final PlayerMock op = server.addPlayer();
		op.setOp(true);

		give.onCommand(op, null, "fequip", new String[]{"hose"});

		assertTrue(op.getInventory().contains(Material.GOLDEN_HOE));
	}

	@Test
	void giveRejectsPlayersWithoutPermission() {
		final PlayerMock player = server.addPlayer();

		give.onCommand(player, null, "fequip", new String[]{"hose"});

		assertFalse(player.getInventory().contains(Material.GOLDEN_HOE));
	}

	@Test
	void consoleGetsAnAnswer() {
		give.onCommand(server.getConsoleSender(), null, "fequip", new String[]{"hose"});

		assertNotNull(server.getConsoleSender().nextMessage());
	}
}

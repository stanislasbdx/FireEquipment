package fr.stan1712.wetston.fireequipment.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolTypeTest {
	@Test
	void resolvesArgumentsIgnoringCase() {
		assertEquals(ToolType.HOSE, ToolType.fromArgument("HoSe").orElseThrow());
		assertEquals(ToolType.EXTINGUISHER, ToolType.fromArgument("extinguisher").orElseThrow());
		assertTrue(ToolType.fromArgument("sword").isEmpty());
	}

	@Test
	void resolvesConfigKeysExactly() {
		assertEquals(ToolType.PUMP, ToolType.fromConfigKey("Pump").orElseThrow());
		assertTrue(ToolType.fromConfigKey("pump").isEmpty());
	}

	@Test
	void buildsConfigPaths() {
		assertEquals("Equipment.Hose.range", ToolType.HOSE.configPath("range"));
		assertEquals("Core.GiveMsg.Pump", ToolType.PUMP.giveMessagePath());
	}
}

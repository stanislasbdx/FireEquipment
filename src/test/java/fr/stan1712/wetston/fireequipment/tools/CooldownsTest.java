package fr.stan1712.wetston.fireequipment.tools;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CooldownsTest {
	@Test
	void blocksUntilCooldownElapsed() {
		final Cooldowns cooldowns = new Cooldowns();
		final UUID id = UUID.randomUUID();

		assertTrue(cooldowns.tryUse(id, 1000, 500));
		assertFalse(cooldowns.tryUse(id, 1200, 500));
		assertTrue(cooldowns.tryUse(id, 1500, 500));
	}

	@Test
	void playersAreIndependent() {
		final Cooldowns cooldowns = new Cooldowns();

		assertTrue(cooldowns.tryUse(UUID.randomUUID(), 1000, 500));
		assertTrue(cooldowns.tryUse(UUID.randomUUID(), 1000, 500));
	}

	@Test
	void clearResetsPlayer() {
		final Cooldowns cooldowns = new Cooldowns();
		final UUID id = UUID.randomUUID();

		cooldowns.tryUse(id, 1000, 500);
		cooldowns.clear(id);

		assertTrue(cooldowns.tryUse(id, 1100, 500));
	}
}

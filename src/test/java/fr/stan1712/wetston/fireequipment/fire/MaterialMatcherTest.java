package fr.stan1712.wetston.fireequipment.fire;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaterialMatcherTest {
	@BeforeEach
	void setUp() {
		MockBukkit.mock();
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void wildcardsAcceptNamespaceAndIgnoreRegexCharacters() {
		assertTrue(MaterialMatcher.resolve(List.of("minecraft:*_stained_glass")).contains(Material.RED_STAINED_GLASS));
		assertTrue(MaterialMatcher.resolve(List.of("(oak*")).isEmpty());
	}
}

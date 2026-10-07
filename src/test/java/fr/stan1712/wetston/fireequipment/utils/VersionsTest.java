package fr.stan1712.wetston.fireequipment.utils;

import fr.stan1712.wetston.fireequipment.utils.Versions.Support;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionsTest {
	@Test
	void yearBasedVersionsAreSupported() {
		assertEquals(Support.SUPPORTED, Versions.gameSupport("26.2"));
		assertEquals(Support.SUPPORTED, Versions.gameSupport("26.1.2"));
		assertEquals(Support.SUPPORTED, Versions.gameSupport("27.1"));
		assertEquals(Support.SUPPORTED, Versions.gameSupport("26.2-R0.1-SNAPSHOT"));
	}

	@Test
	void legacyVersionsAreClassified() {
		assertEquals(Support.SUPPORTED, Versions.gameSupport("1.21.4"));
		assertEquals(Support.SUPPORTED, Versions.gameSupport("1.20"));
		assertEquals(Support.PARTIAL, Versions.gameSupport("1.19.4"));
		assertEquals(Support.PARTIAL, Versions.gameSupport("1.18.2"));
		assertEquals(Support.UNSUPPORTED, Versions.gameSupport("1.17.1"));
	}

	@Test
	void garbageIsUnsupported() {
		assertEquals(Support.UNSUPPORTED, Versions.gameSupport(null));
		assertEquals(Support.UNSUPPORTED, Versions.gameSupport(""));
		assertEquals(Support.UNSUPPORTED, Versions.gameSupport("abc"));
		assertEquals(Support.UNSUPPORTED, Versions.gameSupport("2.5"));
	}

	@Test
	void serverTypeDetection() {
		assertTrue(Versions.isServerTypeSupported("CraftBukkit", "git-Spigot-abc (MC: 26.2)"));
		assertTrue(Versions.isServerTypeSupported("Paper", "git-Paper-12 (MC: 26.2)"));
		assertTrue(Versions.isServerTypeSupported("Purpur", "git-Purpur-1 (MC: 26.2)"));
		assertFalse(Versions.isServerTypeSupported("Foo", "git-Bar-1"));
	}

	@Test
	void compareIsNumeric() {
		assertEquals(0, Versions.compare("2.1.0", "2.1.0"));
		assertEquals(0, Versions.compare("2.1", "2.1.0"));
		assertTrue(Versions.compare("2.1.0", "2.0.9") > 0);
		assertTrue(Versions.compare("2.1.0", "2.10.0") < 0);
		assertTrue(Versions.compare("2.2.0", "2.1.0") > 0);
		assertEquals(0, Versions.compare("2.1.0-SNAPSHOT", "2.1.0"));
	}
}

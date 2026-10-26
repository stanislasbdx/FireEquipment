package fr.stan1712.wetston.fireequipment.fire;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockKeyTest {
	@Test
	void packRoundTripsExtremeCoordinates() {
		final UUID world = UUID.randomUUID();

		for(BlockKey key : List.of(
				new BlockKey(world, 0, 0, 0),
				new BlockKey(world, -1, -64, -1),
				new BlockKey(world, 123456, 319, -654321),
				new BlockKey(world, -30_000_000, -2048, 30_000_000),
				new BlockKey(world, 30_000_000, 2047, -30_000_000))) {
			assertEquals(key, BlockKey.unpack(world, key.pack()));
		}
	}
}

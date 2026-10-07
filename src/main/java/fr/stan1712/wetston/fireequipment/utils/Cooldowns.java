package fr.stan1712.wetston.fireequipment.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cooldowns {
	private final Map<UUID, Long> lastUse = new ConcurrentHashMap<>();

	public boolean tryUse(UUID id, long nowMillis, long cooldownMillis) {
		final Long previous = lastUse.get(id);
		if(previous != null && nowMillis - previous < cooldownMillis) return false;

		lastUse.put(id, nowMillis);
		return true;
	}

	public void clear(UUID id) {
		lastUse.remove(id);
	}
}

package fr.stan1712.wetston.fireequipment.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Versions {
	private static final Pattern GAME_VERSION = Pattern.compile("^(\\d+)\\.(\\d+)");
	private static final int FIRST_YEAR_BASED_MAJOR = 26;
	private static final int MIN_LEGACY_MINOR = 21;

	private Versions() {
		throw new IllegalStateException("Utility class");
	}

	public enum Support {
		SUPPORTED,
		UNSUPPORTED
	}

	public static Support gameSupport(String minecraftVersion) {
		if(minecraftVersion == null) return Support.UNSUPPORTED;

		final Matcher matcher = GAME_VERSION.matcher(minecraftVersion.trim());
		if(!matcher.find()) return Support.UNSUPPORTED;

		final int major = Integer.parseInt(matcher.group(1));
		final int minor = Integer.parseInt(matcher.group(2));

		if(major >= FIRST_YEAR_BASED_MAJOR) return Support.SUPPORTED;
		if(major != 1) return Support.UNSUPPORTED;

		return minor >= MIN_LEGACY_MINOR ? Support.SUPPORTED : Support.UNSUPPORTED;
	}

	public static boolean isServerTypeSupported(String serverName, String serverVersion) {
		final String identity = (serverName + " " + serverVersion).toLowerCase();

		return identity.contains("spigot") || identity.contains("craftbukkit") || identity.contains("paper") || identity.contains("purpur");
	}

	public static int compare(String left, String right) {
		final String[] leftParts = left.trim().split("\\.");
		final String[] rightParts = right.trim().split("\\.");
		final int length = Math.max(leftParts.length, rightParts.length);

		for(int i = 0; i < length; i++) {
			final int leftValue = i < leftParts.length ? leadingNumber(leftParts[i]) : 0;
			final int rightValue = i < rightParts.length ? leadingNumber(rightParts[i]) : 0;

			if(leftValue != rightValue) return Integer.compare(leftValue, rightValue);
		}

		return 0;
	}

	private static int leadingNumber(String part) {
		int end = 0;
		while(end < part.length() && Character.isDigit(part.charAt(end))) end++;

		return end == 0 ? 0 : Integer.parseInt(part.substring(0, end));
	}
}

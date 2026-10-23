package fr.stan1712.wetston.fireequipment.messages;

import java.util.Map;

final class LegacyToMiniMessage {
	private static final Map<Character, String> TAGS = Map.ofEntries(
		Map.entry('0', "<reset><black>"),
		Map.entry('1', "<reset><dark_blue>"),
		Map.entry('2', "<reset><dark_green>"),
		Map.entry('3', "<reset><dark_aqua>"),
		Map.entry('4', "<reset><dark_red>"),
		Map.entry('5', "<reset><dark_purple>"),
		Map.entry('6', "<reset><gold>"),
		Map.entry('7', "<reset><gray>"),
		Map.entry('8', "<reset><dark_gray>"),
		Map.entry('9', "<reset><blue>"),
		Map.entry('a', "<reset><green>"),
		Map.entry('b', "<reset><aqua>"),
		Map.entry('c', "<reset><red>"),
		Map.entry('d', "<reset><light_purple>"),
		Map.entry('e', "<reset><yellow>"),
		Map.entry('f', "<reset><white>"),
		Map.entry('k', "<obfuscated>"),
		Map.entry('l', "<bold>"),
		Map.entry('m', "<strikethrough>"),
		Map.entry('n', "<underlined>"),
		Map.entry('o', "<italic>"),
		Map.entry('r', "<reset>")
	);

	private LegacyToMiniMessage() {
		throw new IllegalStateException("Utility class");
	}

	static String convert(String text) {
		final StringBuilder out = new StringBuilder(text.length() + 16);

		for(int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			final String tag = (c == '&' || c == '§') && i + 1 < text.length() ? TAGS.get(Character.toLowerCase(text.charAt(i + 1))) : null;

			if(tag == null) {
				out.append(c);
			} else {
				out.append(tag);
				i++;
			}
		}

		return out.toString();
	}
}

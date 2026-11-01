package com.anyboot.anynocmds.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Map<Character, String> LEGACY_CODE_TO_MINI_MESSAGE = Map.ofEntries(
            Map.entry('0', "<black>"),
            Map.entry('1', "<dark_blue>"),
            Map.entry('2', "<dark_green>"),
            Map.entry('3', "<dark_aqua>"),
            Map.entry('4', "<dark_red>"),
            Map.entry('5', "<dark_purple>"),
            Map.entry('6', "<gold>"),
            Map.entry('7', "<gray>"),
            Map.entry('8', "<dark_gray>"),
            Map.entry('9', "<blue>"),
            Map.entry('a', "<green>"),
            Map.entry('b', "<aqua>"),
            Map.entry('c', "<red>"),
            Map.entry('d', "<light_purple>"),
            Map.entry('e', "<yellow>"),
            Map.entry('f', "<white>"),
            Map.entry('k', "<obfuscated>"),
            Map.entry('l', "<bold>"),
            Map.entry('m', "<strikethrough>"),
            Map.entry('n', "<underlined>"),
            Map.entry('o', "<italic>"),
            Map.entry('r', "<reset>")
    );

    // Matches &#RRGGBB (legacy hex with #)
    private static final Pattern HASH_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    /**
     * Parses a raw string that may contain MiniMessage tags (<red>, <#hex>, <gradient>, etc.)
     * AND legacy formatting codes (&c, &6, §c, &#RRGGBB, #RRGGBB, etc.).
     * Returns an Adventure {@link Component} ready to be sent to a player.
     */
    public static Component parse(String input) {
        if (input == null) {
            return Component.empty();
        }

        String processed = input.replace('§', '&');

        // Replace &#RRGGBB -> <#RRGGBB>
        Matcher hashMatcher = HASH_PATTERN.matcher(processed);
        processed = hashMatcher.replaceAll("<#$1>");

        // Replace raw hex color codes like #RRGGBB if not inside <...>
        StringBuilder sb = new StringBuilder();
        boolean inTag = false;
        int len = processed.length();
        for (int i = 0; i < len; i++) {
            char c = processed.charAt(i);
            if (c == '<') {
                inTag = true;
            } else if (c == '>') {
                inTag = false;
            }

            if (!inTag && c == '#' && i + 6 < len) {
                boolean isHex = true;
                for (int j = 1; j <= 6; j++) {
                    char hexChar = processed.charAt(i + j);
                    if (!((hexChar >= '0' && hexChar <= '9') || (hexChar >= 'a' && hexChar <= 'f') || (hexChar >= 'A' && hexChar <= 'F'))) {
                        isHex = false;
                        break;
                    }
                }
                if (isHex) {
                    sb.append("<#").append(processed.substring(i + 1, i + 7)).append(">");
                    i += 6;
                    continue;
                }
            }
            sb.append(c);
        }
        processed = sb.toString();

        // Convert legacy color/format codes to MiniMessage tags
        StringBuilder result = new StringBuilder(processed.length());
        for (int i = 0; i < processed.length(); i++) {
            char current = processed.charAt(i);
            if (current == '&' && i + 1 < processed.length()) {
                char code = Character.toLowerCase(processed.charAt(i + 1));
                String replacement = LEGACY_CODE_TO_MINI_MESSAGE.get(code);
                if (replacement != null) {
                    result.append(replacement);
                    i++;
                    continue;
                }
            }
            result.append(current);
        }

        return MINI_MESSAGE.deserialize(result.toString());
    }
}
/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.util;

import java.util.regex.Pattern;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyFormat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Basic color related utilities.
 *
 * @since 1.0.0
 */
@DefaultQualifier(NonNull.class)
public final class ColorUtils {

    private static final Pattern spigotLegacyRGB =
        Pattern.compile("[§&]x[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])");
    private static final Pattern pluginRGB =
        Pattern.compile("[§&]#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])");
    private static final String hexReplacement = "<#$1$2$3$4$5$6>";

    private ColorUtils() {

    }

    /**
     * Parses the input into a color.<br>
     * Supports named colors, legacy, and hex inputs.
     *
     * @param input the color input
     * @return the color
     * @since 1.0.0
     */
    public static @Nullable TextColor parseColor(String input) {
        if (input.isEmpty()) {
            return NamedTextColor.WHITE;
        }

        for (final NamedTextColor namedColor : NamedTextColor.NAMES.values()) {
            if (namedColor.toString().equalsIgnoreCase(input)) {
                return namedColor;
            }
        }

        if (input.contains("&") || input.contains("§")) {
            input = input.replace("&", "§");

            return LegacyComponentSerializer.legacySection().deserialize(input).color();
        }

        return TextColor.fromCSSHexString(input);
    }

    /**
     * Converts the input legacy, legacy rgb, and alternate color formats to MiniMessage color tags.
     *
     * @param input the message to convert
     * @return the converted message
     * @since 1.0.0
     */
    public static String legacyToMiniMessage(final String input) {
        String output = input;

        // Legacy RGB
        output = spigotLegacyRGB.matcher(output).replaceAll(hexReplacement);

        // Alternate RGB, TAB (neznamy) && KiteBoard
        output = pluginRGB.matcher(output).replaceAll(hexReplacement);

        // Legacy Colors
        for (final char c : "0123456789abcdefABCDEF".toCharArray()) {
            final @Nullable LegacyFormat format = LegacyComponentSerializer.parseChar(Character.toLowerCase(c));

            if (format != null) {
                final @Nullable TextColor color = format.color();

                if (color != null) {
                    output = output.replaceAll("[§&]" + c, "<" + color.asHexString() + ">");
                }
            }
        }

        // Legacy Formatting
        for (final char c : "klmnoKLMNO".toCharArray()) {
            final @Nullable LegacyFormat format = LegacyComponentSerializer.parseChar(Character.toLowerCase(c));

            if (format != null) {
                final @Nullable TextDecoration decoration = format.decoration();

                if (decoration != null) {
                    output = output.replaceAll("[§&]" + c, "<" + decoration.name() + ">");
                }
            }
        }

        return output;
    }

}

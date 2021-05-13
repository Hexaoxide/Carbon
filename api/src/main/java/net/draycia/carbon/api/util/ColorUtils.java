package net.draycia.carbon.api.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyFormat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Pattern;

/**
 * Basic color related utilities.
 *
 * @since 1.0.0
 */
public final class ColorUtils {

  private ColorUtils() {

  }

  /**
   * Parses the input into a color.<br>
   * Supports named colors, legacy, and hex inputs.
   *
   * @param input the color input
   *
   * @return the color
   *
   * @since 1.0.0
   */
  public static @Nullable TextColor parseColor(@NonNull String input) {
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

  private static final @NonNull Pattern spigotLegacyRGB =
    Pattern.compile("[§&]x[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])");

  private static final @NonNull Pattern pluginRGB =
    Pattern.compile("[§&]#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])");

  private static final @NonNull String hexReplacement = "<#$1$2$3$4$5$6>";

  /**
   * Converts the input legacy, legacy rgb, and alternate color formats to MiniMessage color tags.
   *
   * @param input the message to convert
   *
   * @return the converted message
   *
   * @since 1.0.0
   */
  public static @NonNull String legacyToMiniMessage(final @NonNull String input) {
    String output = input;

    // Legacy RGB
    output = spigotLegacyRGB.matcher(output).replaceAll(hexReplacement);

    // Alternate RGB, TAB (neznamy) && KiteBoard
    output = pluginRGB.matcher(output).replaceAll(hexReplacement);

    // Legacy Colors
    for (final char c : "0123456789abcdefABCDEF".toCharArray()) {
      final LegacyFormat format = LegacyComponentSerializer.parseChar(Character.toLowerCase(c));

      if (format != null) {
        final TextColor color = format.color();

        if (color != null) {
          output = output.replaceAll("[§&]" + c, "<" + color.asHexString() + ">");
        }
      }
    }

    // Legacy Formatting
    for (final char c : "klmnoKLMNO".toCharArray()) {
      final LegacyFormat format = LegacyComponentSerializer.parseChar(Character.toLowerCase(c));

      if (format != null) {
        final TextDecoration decoration = format.decoration();

        if (decoration != null) {
          output = output.replaceAll("[§&]" + c, "<" + decoration.name() + ">");
        }
      }
    }

    return output;
  }

}

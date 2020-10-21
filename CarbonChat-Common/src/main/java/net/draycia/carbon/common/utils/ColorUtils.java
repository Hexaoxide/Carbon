package net.draycia.carbon.common.utils;

import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.adventure.AdventureManager;
import net.draycia.carbon.api.adventure.FormatType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyFormat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Pattern;

public final class ColorUtils {

  private ColorUtils() {

  }

  public @Nullable static TextColor parseColor(final @Nullable String input) {
    return parseColor(null, input);
  }

  public @Nullable static TextColor parseColor(final @Nullable CarbonUser user, @Nullable String input) {
    if (user instanceof PlayerUser) {
      input = ((PlayerUser) user).parsePlaceholders(input);
    }

    if (input == null || input.isEmpty()) {
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
    Pattern.compile("[§&]x[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])");

  private static final @NonNull Pattern pluginRGB =
    Pattern.compile("[§&]#([0-9a-f])([0-9a-f])([0-9a-f])([0-9a-f])([0-9a-f])([0-9a-f])");

  @NonNull
  public static String translateAlternateColors(final @NonNull String input) {
    // TODO: check if MiniMessage or MineDown
    String output = input;

    String hexReplacement = "<#$1$2$3$4$5$6>";
    boolean legacyCapable = false;

    final MessageProcessor messageProcessor = CarbonChatProvider.carbonChat().messageProcessor();

    if (messageProcessor instanceof AdventureManager) {
      if (messageProcessor.formatType() == FormatType.MINEDOWN) {
        hexReplacement = "&#$1$2$3$4$5$6&";
        legacyCapable = true;
      }
    }

    // Legacy RGB
    output = spigotLegacyRGB.matcher(output).replaceAll(hexReplacement);

    // Alternate RGB, TAB (neznamy) && KiteBoard
    output = pluginRGB.matcher(output).replaceAll(hexReplacement);

    if (!legacyCapable) {
      // Legacy Colors
      for (final char c : "0123456789abcdef".toCharArray()) {
        final LegacyFormat format = LegacyComponentSerializer.parseChar(c);
        final TextColor color = format.color();

        output = output.replaceAll("[§&]" + c, "<" + color.asHexString() + ">");
      }

      // Legacy Formatting
      for (final char c : "klmno".toCharArray()) {
        final LegacyFormat format = LegacyComponentSerializer.parseChar(c);
        final TextDecoration decoration = format.decoration();

        output = output.replaceAll("[§&]" + c, "<" + decoration.name() + ">");
      }
    }

    return output;
  }

}

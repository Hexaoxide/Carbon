package net.draycia.carbon.common.utils;

import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ColorUtils {

  private ColorUtils() {

  }

  public @Nullable static TextColor parseColor(final @Nullable String input) {
    return parseColor(null, input);
  }

  public @Nullable static TextColor parseColor(final @Nullable ChatUser user, @Nullable String input) {
    if (input == null || input.isEmpty()) {
      return NamedTextColor.WHITE;
    }

    // TODO: find out way to do this

    //    if (user != null) {
    //      final Player player = Bukkit.getPlayer(user.uuid());
    //
    //      if (player != null) {
    //        input = PlaceholderAPI.setPlaceholders(player, input);
    //      }
    //    }

    for (final NamedTextColor namedColor : NamedTextColor.values()) {
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

}

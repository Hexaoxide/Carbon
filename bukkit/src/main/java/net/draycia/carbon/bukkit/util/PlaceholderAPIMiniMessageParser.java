package net.draycia.carbon.bukkit.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;

public final class PlaceholderAPIMiniMessageParser {

  private final MiniMessage miniMessage;

  private PlaceholderAPIMiniMessageParser(final @NonNull MiniMessage miniMessage) {
    this.miniMessage = miniMessage;
  }

  public @NonNull Component parse(final @NonNull OfflinePlayer player, final @NonNull String input, final @NonNull Collection<Template> templates) {
    return this.parse(
      PlaceholderAPI.getPlaceholderPattern(),
      match -> PlaceholderAPI.setPlaceholders(player, match),
      input,
      templates
    );
  }

  public @NonNull Component parse(final @NonNull OfflinePlayer player, final @NonNull String input) {
    return this.parse(player, input, emptyList());
  }

  public @NonNull Component parseRelational(final @NonNull Player one, final @NonNull Player two, final @NonNull String input, final @NonNull Collection<Template> templates) {
    return this.parse(
      PlaceholderAPI.getPlaceholderPattern(),
      match -> PlaceholderAPI.setPlaceholders(one, PlaceholderAPI.setRelationalPlaceholders(one, two, match)),
      input,
      templates
    );
  }

  public @NonNull Component parseRelational(final @NonNull Player one, final @NonNull Player two, final @NonNull String input) {
    return this.parseRelational(one, two, input, emptyList());
  }

  private @NonNull Component parse(
    final @NonNull Pattern pattern,
    final @NonNull UnaryOperator<String> placeholderResolver,
    final @NonNull String input,
    final @NonNull Collection<Template> originalTemplates
  ) {
    final Matcher matcher = pattern.matcher(input);
    final List<Template> templates = new ArrayList<>(originalTemplates);
    final StringBuilder builder = new StringBuilder();
    int id = 0;
    while (matcher.find()) {
      final String match = matcher.group();
      final String replaced = placeholderResolver.apply(match);
      if (match.equals(replaced) || !containsLegacyColorCodes(replaced)) {
        matcher.appendReplacement(builder, replaced);
      } else {
        final String key = "papi_generated_template_" + id;
        id++;
        templates.add(Template.of(key, LegacyComponentSerializer.legacySection().deserialize(replaced)));
        matcher.appendReplacement(builder, "<" + key + ">");
      }
    }
    matcher.appendTail(builder);
    return this.miniMessage.parse(builder.toString(), templates);
  }

  public static @NonNull PlaceholderAPIMiniMessageParser create(final @NonNull MiniMessage backingInstance) {
    return new PlaceholderAPIMiniMessageParser(backingInstance);
  }

  private static boolean containsLegacyColorCodes(final @NonNull String string) {
    final char[] charArray = string.toCharArray();
    for (int i = 0; i < charArray.length; i++) {
      if (charArray[i] == LegacyComponentSerializer.SECTION_CHAR) {
        return true;
      }
    }
    return false;
  }

}

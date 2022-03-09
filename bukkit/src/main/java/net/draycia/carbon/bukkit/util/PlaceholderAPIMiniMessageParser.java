/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
package net.draycia.carbon.bukkit.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Collections.emptyList;

@DefaultQualifier(NonNull.class)
public final class PlaceholderAPIMiniMessageParser {

    private final MiniMessage miniMessage;

    private PlaceholderAPIMiniMessageParser(final MiniMessage miniMessage) {
        this.miniMessage = miniMessage;
    }

    public static PlaceholderAPIMiniMessageParser create(final MiniMessage backingInstance) {
        return new PlaceholderAPIMiniMessageParser(backingInstance);
    }

    private static boolean containsLegacyColorCodes(final String string) {
        final char[] charArray = string.toCharArray();

        for (final char c : charArray) {
            if (c == LegacyComponentSerializer.SECTION_CHAR) {
                return true;
            }
        }

        return false;
    }

    public Component parse(final OfflinePlayer player, final String input, final Collection<TagResolver> placeholders) {
        return this.parse(
            PlaceholderAPI.getPlaceholderPattern(),
            match -> PlaceholderAPI.setPlaceholders(player, match),
            input,
            placeholders
        );
    }

    public Component parse(final OfflinePlayer player, final String input) {
        return this.parse(player, input, emptyList());
    }

    public Component parseRelational(final Player one, final Player two, final String input, final Collection<TagResolver> placeholders) {
        return this.parse(
            PlaceholderAPI.getPlaceholderPattern(),
            match -> PlaceholderAPI.setPlaceholders(one, PlaceholderAPI.setRelationalPlaceholders(one, two, match)),
            input,
            placeholders
        );
    }

    public Component parseRelational(final Player one, final Player two, final String input) {
        return this.parseRelational(one, two, input, emptyList());
    }

    private Component parse(
        final Pattern pattern,
        final UnaryOperator<String> placeholderResolver,
        final String input,
        final Collection<TagResolver> originalPlaceholders
    ) {
        final Matcher matcher = pattern.matcher(input);
        final List<TagResolver> placeholders = new ArrayList<>(originalPlaceholders);
        final StringBuilder builder = new StringBuilder();
        int id = 0;

        while (matcher.find()) {
            final String match = matcher.group();
            final String replaced = placeholderResolver.apply(match);

            if (match.equals(replaced) || !containsLegacyColorCodes(replaced)) {
                matcher.appendReplacement(builder, Matcher.quoteReplacement(replaced));
            } else {
                final String key = "papi_generated_template_" + id;
                id++;
                placeholders.add(Placeholder.component(key, LegacyComponentSerializer.legacySection().deserialize(replaced)));
                matcher.appendReplacement(builder, Matcher.quoteReplacement("<" + key + ">"));
            }
        }

        matcher.appendTail(builder);

        return this.miniMessage.deserialize(builder.toString(), TagResolver.resolver(placeholders));
    }

}

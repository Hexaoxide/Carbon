/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.messages;

import java.util.Map;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class TagPermissions {

    public static final String NICKNAME = "carbon.nickname.tags";
    public static final String MESSAGE = "carbon.messagetags";
    public static final String PARTY_NAME = "carbon.parties.name.tags";
    private static final Map<String, TagResolver> DEFAULT_TAGS = Map.ofEntries(
        Map.entry("hover", StandardTags.hoverEvent()),
        Map.entry("click", StandardTags.clickEvent()),
        Map.entry("color", StandardTags.color()),
        Map.entry("keybind", StandardTags.keybind()),
        Map.entry("translatable", StandardTags.translatable()),
        Map.entry("insertion", StandardTags.insertion()),
        Map.entry("font", StandardTags.font()),
        Map.entry("decorations", StandardTags.decorations()),
        Map.entry("gradient", StandardTags.gradient()),
        Map.entry("rainbow", StandardTags.rainbow()),
        Map.entry("reset", StandardTags.reset()),
        Map.entry("newline", StandardTags.newline())
    );

    private TagPermissions() {
    }

    public static Component parseTags(final String basePermission, final String message, final Predicate<String> permission, final TagResolver.Builder resolver) {
        boolean hasAllDecorations = false;
        for (final Map.Entry<String, TagResolver> entry : DEFAULT_TAGS.entrySet()) {
            if (permission.test(basePermission + '.' + entry.getKey())) {
                resolver.resolver(entry.getValue());
                if (entry.getKey().equals("decorations")) {
                    hasAllDecorations = true;
                }
            }
        }

        if (!hasAllDecorations) {
            for (final TextDecoration decoration : TextDecoration.values()) {
                if (!permission.test(basePermission + '.' + decoration.name())) {
                    continue;
                }

                resolver.resolver(StandardTags.decorations(decoration));
            }
        }

        final MiniMessage miniMessage = MiniMessage.builder().tags(resolver.build()).build();

        return miniMessage.deserialize(message);
    }

    public static Component parseTags(final String basePermission, final String message, final Predicate<String> permission) {
        return parseTags(basePermission, message, permission, TagResolver.builder());
    }

}

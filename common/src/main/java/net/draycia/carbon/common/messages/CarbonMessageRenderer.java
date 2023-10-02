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
package net.draycia.carbon.common.messages;

import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.moonshine.message.IMessageRenderer;

@FunctionalInterface
public interface CarbonMessageRenderer extends IMessageRenderer<Audience, String, Component, Object> {

    static void addResolved(final TagResolver.Builder tagResolver, final Map<String, ?> resolvedPlaceholders) {
        for (final var entry : resolvedPlaceholders.entrySet()) {
            if (entry.getValue() instanceof Tag tag) {
                tagResolver.tag(entry.getKey(), tag);
            } else if (entry.getValue() instanceof TagResolver resolver) {
                tagResolver.resolver(resolver);
            } else {
                throw new IllegalArgumentException(entry.getValue().toString());
            }
        }
    }

    default IMessageRenderer<SourcedAudience, String, Component, Object> asSourced() {
        return this::render;
    }

}

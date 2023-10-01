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
package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messages.CarbonMessageRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public class SpongeMessageRenderer implements CarbonMessageRenderer {

    private final ConfigManager configManager;

    @Inject
    public SpongeMessageRenderer(final ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Component render(
        final Audience receiver,
        final String intermediateMessage,
        final Map<String, ? extends Tag> resolvedPlaceholders,
        final Method method,
        final Type owner
    ) {
        final TagResolver.Builder tagResolver = TagResolver.builder();

        for (final var entry : resolvedPlaceholders.entrySet()) {
            tagResolver.tag(entry.getKey(), entry.getValue());
        }

        this.configManager.primaryConfig().customPlaceholders().forEach(
            (key, value) -> tagResolver.resolver(Placeholder.unparsed(key, value))
        );

        // TODO: MiniPlaceholders support

        return MiniMessage.miniMessage().deserialize(intermediateMessage, tagResolver.build());
    }

}

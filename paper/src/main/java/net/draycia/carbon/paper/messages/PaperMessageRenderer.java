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
package net.draycia.carbon.paper.messages;

import com.google.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.paper.CarbonChatPaper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.moonshine.message.IMessageRenderer;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class PaperMessageRenderer<T extends Audience> implements IMessageRenderer<T, String, Component, Component> {

    private @MonotonicNonNull PlaceholderAPIMiniMessageParser parser = null;

    private final MiniMessage miniMessage;
    private final ConfigFactory configFactory;

    @Inject
    public PaperMessageRenderer(final ConfigFactory configFactory) {
        this.miniMessage = MiniMessage.miniMessage();
        this.configFactory = configFactory;

        if (CarbonChatPaper.papiLoaded()) {
            this.parser = PlaceholderAPIMiniMessageParser.create(MiniMessage.miniMessage());
        }
    }

    @Override
    public Component render(
        final T receiver,
        final String intermediateMessage,
        final Map<String, ? extends Component> resolvedPlaceholders,
        final Method method,
        final Type owner
    ) {
        final TagResolver.Builder tagResolver = TagResolver.builder();

        for (final var entry : resolvedPlaceholders.entrySet()) {
            tagResolver.tag(entry.getKey(), Tag.inserting(entry.getValue()));
        }

        // https://github.com/KyoriPowered/adventure-text-minimessage/issues/131
        // TLDR: 25/10/21, tags in templates aren't parsed. we want them parsed.
        String placeholderResolvedMessage = intermediateMessage;

        for (final var entry : this.configFactory.primaryConfig().customPlaceholders().entrySet()) {
            placeholderResolvedMessage = placeholderResolvedMessage.replace("<" + entry.getKey() + ">",
                entry.getValue());
        }

        final Component message;

        if (receiver instanceof SourcedAudience sourced && this.parser != null) {
            if (sourced.sender() instanceof CarbonPlayer sender && sender.online()) {
                if (sourced.recipient() instanceof CarbonPlayer recipient && recipient.online()) {
                    message = this.parser.parseRelational(Bukkit.getPlayer(sender.uuid()),
                        Bukkit.getPlayer(recipient.uuid()), placeholderResolvedMessage, tagResolver.build());
                } else {
                    message = this.parser.parse(Bukkit.getPlayer(sender.uuid()), placeholderResolvedMessage, tagResolver.build());
                }
            } else {
                message = this.miniMessage.deserialize(placeholderResolvedMessage, tagResolver.build());
            }
        } else {
            message = this.miniMessage.deserialize(placeholderResolvedMessage, tagResolver.build());
        }

        return message;
    }

}

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

import com.google.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.bukkit.CarbonChatBukkit;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.util.ChatType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.moonshine.message.IMessageRenderer;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class BukkitMessageRenderer<T extends Audience> implements IMessageRenderer<T, String, RenderedMessage, Component> {

    private @MonotonicNonNull PlaceholderAPIMiniMessageParser parser = null;

    private final MiniMessage miniMessage;
    private final ConfigFactory configFactory;

    @Inject
    public BukkitMessageRenderer(final ConfigFactory configFactory) {
        this.miniMessage = MiniMessage.miniMessage();
        this.configFactory = configFactory;

        if (((CarbonChatBukkit) CarbonChatProvider.carbonChat()).papiLoaded()) {
            this.parser = PlaceholderAPIMiniMessageParser.create(MiniMessage.miniMessage());
        }
    }

    @Override
    public RenderedMessage render(
        final T receiver,
        final String intermediateMessage,
        final Map<String, ? extends Component> resolvedPlaceholders,
        final Method method,
        final Type owner
    ) {
        final List<TagResolver> placeholders = new ArrayList<>();

        for (final var entry : resolvedPlaceholders.entrySet()) {
            placeholders.add(Placeholder.component(entry.getKey(), entry.getValue()));
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
                        Bukkit.getPlayer(recipient.uuid()), placeholderResolvedMessage, placeholders);
                } else {
                    message = this.parser.parse(Bukkit.getPlayer(sender.uuid()), placeholderResolvedMessage, placeholders);
                }
            } else {
                message = this.miniMessage.deserialize(placeholderResolvedMessage, TagResolver.resolver(placeholders));
            }
        } else {
            message = this.miniMessage.deserialize(placeholderResolvedMessage, TagResolver.resolver(placeholders));
        }

        final MessageType messageType;
        final @Nullable ChatType chatType = method.getAnnotation(ChatType.class);

        if (chatType != null) {
            messageType = chatType.value();
        } else {
            messageType = MessageType.SYSTEM;
        }

        return new RenderedMessage(message, messageType);
    }

}

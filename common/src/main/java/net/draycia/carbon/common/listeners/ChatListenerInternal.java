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
package net.draycia.carbon.common.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.draycia.carbon.api.channels.ChannelPermissionResult;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonChatEventImpl;
import net.draycia.carbon.common.event.events.CarbonEarlyChatEvent;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messages.TagPermissions;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class ChatListenerInternal {

    private final ConfigManager configManager;
    private final CarbonMessages carbonMessages;
    private final CarbonEventHandler carbonEventHandler;

    protected ChatListenerInternal(
        final CarbonEventHandler carbonEventHandler,
        final CarbonMessages carbonMessages,
        final ConfigManager configManager
    ) {
        this.configManager = configManager;
        this.carbonMessages = carbonMessages;
        this.carbonEventHandler = carbonEventHandler;
    }

    protected @Nullable CarbonChatEventImpl prepareAndEmitChatEvent(final CarbonPlayer sender, final Component originalMessage, final @Nullable SignedMessage signedMessage) {
        final CarbonPlayer.ChannelMessage channelMessage = sender.channelForMessage(originalMessage);
        final ChatChannel channel = channelMessage.channel();

        Component message = originalMessage;

        final String prefix = channel.quickPrefix();
        if (prefix != null) {
            message = originalMessage.replaceText(TextReplacementConfig.builder()
                .once()
                .matchLiteral(prefix)
                .replacement(Component.empty())
                .build());
        }

        return this.prepareAndEmitChatEvent(sender, message, signedMessage, channel);
    }

    protected @Nullable CarbonChatEventImpl prepareAndEmitChatEvent(final CarbonPlayer sender, final Component message, final @Nullable SignedMessage signedMessage, final ChatChannel channel) {
        if (!sender.hasPermission("carbon.cooldown.exempt") && channel.cooldown() > 0) {
            final long currentMillis = System.currentTimeMillis();
            final long expiresAt = channel.playerCooldown(sender);

            if (currentMillis < expiresAt) {
                // Round up, or the player can be told they have 0 seconds remaining
                final long remaining = (long) Math.ceil((double) (expiresAt - currentMillis) / 1000);
                this.carbonMessages.channelCooldown(sender, remaining);
                return null;
            }

            channel.startCooldown(sender);
        }

        if (sender.leftChannels().contains(channel.key())) {
            sender.joinChannel(channel);
            this.carbonMessages.channelJoined(sender);
        }

        final List<KeyedRenderer> renderers = new ArrayList<>();
        renderers.add(KeyedRenderer.keyedRenderer(Key.key("carbon", "default"), channel));

        final List<Audience> recipients = channel.recipients(sender);

        final var chatEvent = new CarbonChatEventImpl(sender, message, recipients, renderers, channel, signedMessage);

        this.carbonEventHandler.emit(chatEvent);

        return chatEvent;
    }

    protected @Nullable CarbonEarlyChatEvent prepareAndEmitPreChatEvent(final CarbonPlayer sender, final Component originalMessage) {
        final CarbonPlayer.ChannelMessage channelMessage = sender.channelForMessage(originalMessage);
        final ChatChannel channel = channelMessage.channel();

        final ChannelPermissionResult permitted = channel.permissions().speechPermitted(sender);
        if (!permitted.permitted()) {
            sender.sendMessage(permitted.reason());
            return null;
        }

        final String plainContent = PlainTextComponentSerializer.plainText().serialize(channelMessage.message());
        final String content = this.configManager.primaryConfig().applyChatPlaceholders(plainContent);

        final CarbonEarlyChatEvent earlyChatEvent = new CarbonEarlyChatEvent(sender, content);
        this.carbonEventHandler.emit(earlyChatEvent);

        return earlyChatEvent;
    }

    protected @Nullable Component parseTags(final CarbonPlayer sender, final String format) {
        final Component message;

        if (sender instanceof WrappedCarbonPlayer wrapped) {
            message = wrapped.parseMessageTags(format);
        } else {
            message = TagPermissions.parseTags(sender, TagPermissions.MESSAGE, format, sender::hasPermission);
        }

        if (probablyBlank(message)) {
            return null;
        }

        return message;
    }

    private static boolean probablyBlank(final Component component) {
        final Iterator<Component> it = component.iterator(ComponentIteratorType.DEPTH_FIRST);
        while (it.hasNext()) {
            final Component c = it.next();
            if (!(c instanceof TextComponent text)) {
                // Assume non-text components probably aren't blank
                return false;
            } else if (!text.content().isBlank()) {
                // Found some content, definitely not blank
                return false;
            }
        }
        // Likely blank
        return true;
    }

}

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
package net.draycia.carbon.paper.listeners;

import com.google.inject.Inject;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.Map;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.draycia.carbon.paper.CarbonChatPaper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import ninja.egg82.messenger.services.PacketService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.draycia.carbon.common.util.Strings.URL_REPLACEMENT_CONFIG;
import static net.kyori.adventure.key.Key.key;

@DefaultQualifier(NonNull.class)
public final class PaperChatListener implements Listener {

    private final CarbonChatPaper carbonChat;
    private final ChannelRegistry registry;
    private final CarbonMessages carbonMessages;
    final ConfigFactory configFactory;

    @Inject
    public PaperChatListener(
        final CarbonChat carbonChat,
        final ChannelRegistry registry,
        final CarbonMessages carbonMessages,
        final ConfigFactory configFactory
    ) {
        this.carbonChat = (CarbonChatPaper) carbonChat;
        this.registry = registry;
        this.carbonMessages = carbonMessages;
        this.configFactory = configFactory;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPaperChat(final @NonNull AsyncChatEvent event) {
        final @Nullable CarbonPlayer sender = this.carbonChat.userManager().user(event.getPlayer().getUniqueId()).join();

        if (event.viewers().isEmpty()) {
            return;
        }

        var channel = requireNonNullElse(sender.selectedChannel(), this.registry.defaultValue());

        String content = PlainTextComponentSerializer.plainText().serialize(event.message());

        for (final Map.Entry<String, String> placeholder : this.configFactory.primaryConfig().chatPlaceholders().entrySet()) {
            content = content.replace(placeholder.getKey(), placeholder.getValue());
        }

        Component eventMessage = ConfigChatChannel.parseMessageTags(sender, content);

        if (sender.hasPermission("carbon.chatlinks")) {
            eventMessage = eventMessage.replaceText(URL_REPLACEMENT_CONFIG.get());
        }

        final CarbonPlayer.ChannelMessage channelMessage = sender.channelForMessage(eventMessage);

        if (channelMessage.channel() != null) {
            channel = channelMessage.channel();
        }

        eventMessage = channelMessage.message();

        if (sender.leftChannels().contains(channel.key())) {
            sender.joinChannel(channel);
            this.carbonMessages.channelJoined(sender);
        }

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var recipients = channel.recipients(sender);
        final var chatEvent = new CarbonChatEvent(sender, eventMessage, recipients, renderers, channel, event.signedMessage());
        this.carbonChat.eventHandler().emit(chatEvent);

        if (chatEvent.cancelled()) {
            event.setCancelled(true);
            return;
        }

        try {
            event.viewers().clear();
            event.viewers().addAll(recipients);
        } catch (final UnsupportedOperationException exception) {
            exception.printStackTrace();
        }

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            var renderedMessage = chatEvent.message();
            final var recipientUUID = viewer.get(Identity.UUID);
            final Audience recipientViewer;

            if (recipientUUID.isPresent()) {
                recipientViewer = this.carbonChat.userManager().user(recipientUUID.get()).join();
            } else {
                recipientViewer = viewer;
            }

            for (final var renderer : chatEvent.renderers()) {
                renderedMessage = renderer.render(sender, recipientViewer, renderedMessage, event.originalMessage());
            }

            return renderedMessage;
        });

        final @Nullable PacketService packetService = this.carbonChat.packetService();

        if (packetService != null) {
            Component networkMessage = chatEvent.message();

            for (final var renderer : chatEvent.renderers()) {
                networkMessage = renderer.render(sender, sender, networkMessage, event.originalMessage());
            }

            packetService.queuePacket(new ChatMessagePacket(this.carbonChat.serverId(), sender.uuid(),
                channel.permission(), channel.key(), sender.username(), networkMessage));
        }
    }

}

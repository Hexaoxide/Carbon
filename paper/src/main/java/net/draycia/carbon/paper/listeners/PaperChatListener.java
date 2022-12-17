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
package net.draycia.carbon.paper.listeners;

import com.google.inject.Inject;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.paper.CarbonChatPaper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.event.EventSubscriber;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public final class PaperChatListener implements Listener {

    private final CarbonChatPaper carbonChat;
    private final ChannelRegistry registry;

    private static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?");

    @Inject
    public PaperChatListener(final CarbonChat carbonChat, final ChannelRegistry registry) {
        this.carbonChat = (CarbonChatPaper) carbonChat;
        this.registry = registry;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSpigotChat(final @NonNull AsyncPlayerChatEvent event) {
        event.setFormat("%2$s");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPaperChat(final @NonNull AsyncChatEvent event) {
        final var playerResult = this.carbonChat.server().userManager().carbonPlayer(event.getPlayer().getUniqueId()).join();
        final @Nullable CarbonPlayer sender = playerResult.player();

        if (sender == null || event.viewers().isEmpty()) {
            return;
        }

        var channel = requireNonNullElse(sender.selectedChannel(), this.registry.defaultValue());
        final var messageContents = PlainTextComponentSerializer.plainText().serialize(event.message());
        Component eventMessage = ConfigChatChannel.parseMessageTags(sender, messageContents);

        if (sender.hasPermission("carbon.chatlinks")) {
            eventMessage = eventMessage.replaceText(TextReplacementConfig.builder()
                .match(DEFAULT_URL_PATTERN)
                .replacement(builder -> builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, builder.content())))
                .build());
        }

        for (final var chatChannel : this.registry) {
            if (chatChannel.quickPrefix() == null) {
                continue;
            }

            if (messageContents.startsWith(chatChannel.quickPrefix()) && chatChannel.speechPermitted(sender).permitted()) {
                channel = chatChannel;
                eventMessage = eventMessage.replaceText(TextReplacementConfig.builder()
                    .once()
                    .matchLiteral(channel.quickPrefix())
                    .replacement(text())
                    .build());
                break;
            }
        }

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var recipients = channel.recipients(sender);
        final var chatEvent = new CarbonChatEvent(sender, eventMessage, recipients, renderers, channel, false);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (!result.wasSuccessful()) {
            for (final Map.Entry<EventSubscriber<?>, Throwable> entry : result.exceptions().entrySet()) {
                this.carbonChat.logger().error(entry.getValue());
            }
        }

        try {
            event.viewers().clear();
            event.viewers().addAll(recipients);
        } catch (final UnsupportedOperationException exception) {
            exception.printStackTrace();
        }

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            var renderedMessage = chatEvent.message();
            var recipientUUID = viewer.get(Identity.UUID);
            final Audience recipientViewer;

            if (recipientUUID.isPresent()) {
                final var recipientResult = this.carbonChat.server().userManager().carbonPlayer(viewer.get(Identity.UUID).orElseThrow()).join();
                final @Nullable CarbonPlayer recipient = recipientResult.player();

                recipientViewer = requireNonNullElse(recipient, viewer);
            } else {
                recipientViewer = viewer;
            }

            for (final var renderer : chatEvent.renderers()) {
                renderedMessage = renderer.render(sender, recipientViewer, renderedMessage, event.originalMessage());
            }

            return renderedMessage;
        });
    }

}

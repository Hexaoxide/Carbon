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
package net.draycia.carbon.paper.listeners;

import com.google.inject.Inject;
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonChatEventImpl;
import net.draycia.carbon.common.event.events.CarbonEarlyChatEvent;
import net.draycia.carbon.common.listeners.ChatListenerInternal;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class PaperChatListener extends ChatListenerInternal implements Listener {

    private final CarbonChat carbonChat;
    final ConfigManager configManager;
    private final Map<UUID, Key> quickPrefixChannels = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> usedQuickPrefix = new ConcurrentHashMap<>();
    // Stores the resolved CarbonPlayer sender from onPaperChatDecorate so that
    // onPaperChat can reuse it without any additional user-manager lookup.  This
    // prevents a second (potentially blocking or null-returning) cache access
    // between the onPaperChatDecorate (LOWEST) and onPaperChat (HIGHEST) events,
    // which is one of the root causes of the
    // "Checksum mismatch on last seen update" Paper disconnect.
    private final Map<UUID, CarbonPlayer> pendingSenders = new ConcurrentHashMap<>();

    @Inject
    public PaperChatListener(
        final CarbonChat carbonChat,
        final CarbonMessages carbonMessages,
        final ConfigManager configManager
    ) {
        super(carbonChat.eventHandler(), carbonMessages, configManager);
        this.carbonChat = carbonChat;
        this.configManager = configManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPaperChatDecorate(final @NonNull AsyncChatDecorateEvent event) {
        if (event.player() == null) {
            return;
        }

        final UUID playerId = event.player().getUniqueId();

        // Use getNow(null) instead of join() to avoid blocking the async chat thread.
        // Blocking here delays message serialization, which desynchronizes Paper's
        // signed-message acknowledgement tracking and causes
        // "Checksum mismatch on last seen update" disconnects.
        // The user is pre-warmed by PaperPlayerJoinListener, so it is virtually
        // always present for online players.  If it is not ready yet (first-ever
        // message after a very quick login), we skip Carbon's early decoration for
        // this single message; onPaperChat at HIGHEST will still handle it or
        // cancel cleanly.
        final @Nullable CarbonPlayer sender = this.carbonChat.userManager().user(playerId).getNow(null);
        if (sender == null) {
            return;
        }

        final @Nullable CarbonEarlyChatEvent earlyChatEvent = this.prepareAndEmitPreChatEvent(sender, event.result());

        if (earlyChatEvent == null || earlyChatEvent.cancelled()) {
            event.setCancelled(true);
            return;
        }

        final @Nullable Component message = this.parseTags(sender, earlyChatEvent.message());

        if (message != null) {
            event.result(message);
        }

        final CarbonPlayer.ChannelMessage channelMessage = sender.channelForMessage(event.originalMessage());
        final ChatChannel channel = channelMessage.channel();
        this.quickPrefixChannels.put(playerId, channel.key());
        final boolean prefixUsed = channelMessage.message() != event.originalMessage();
        this.usedQuickPrefix.put(playerId, prefixUsed);

        // Snapshot the resolved sender so onPaperChat can use it directly.
        this.pendingSenders.put(playerId, sender);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPaperCommandDecorate(final @NonNull AsyncChatCommandDecorateEvent event) {
        this.onPaperChatDecorate(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPaperChat(final @NonNull AsyncChatEvent event) {
        final UUID playerId = event.getPlayer().getUniqueId();

        // Prefer the sender that was already resolved (and validated) by
        // onPaperChatDecorate at LOWEST priority.  This avoids any user-manager
        // lookup in this hot path and guarantees we use a consistent player
        // snapshot throughout the entire chat pipeline.
        // Fall back to a non-blocking cache peek for the rare case where the
        // decoration event was skipped (user not yet loaded at decorate time).
        @Nullable CarbonPlayer sender = this.pendingSenders.remove(playerId);
        if (sender == null) {
            sender = this.carbonChat.userManager().user(playerId).getNow(null);
        }
        if (sender == null) {
            event.setCancelled(true);
            return;
        }

        if (event.viewers().isEmpty()) {
            return;
        }

        final Key channelKey = this.quickPrefixChannels.remove(playerId);
        final ChatChannel channel = channelKey != null
            ? this.carbonChat.channelRegistry().channelOrDefault(channelKey)
            : this.carbonChat.channelRegistry().defaultChannel();
        final boolean prefixUsed = Boolean.TRUE.equals(this.usedQuickPrefix.remove(playerId));
        SignedMessage signedMessage = event.signedMessage();
        final Component decoratedMessage = event.message();
        if (prefixUsed) {
            signedMessage = null;
        }
        final SignedMessage renderSignedMessage = signedMessage;
        final Component messageForEvent = renderSignedMessage != null ? event.originalMessage() : decoratedMessage;
        final @Nullable CarbonChatEventImpl chatEvent = this.prepareAndEmitChatEvent(sender, messageForEvent, renderSignedMessage, channel);

        if (chatEvent == null || chatEvent.cancelled()) {
            event.setCancelled(true);
            return;
        }

        try {
            event.viewers().clear();
            event.viewers().addAll(chatEvent.recipients());
        } catch (final UnsupportedOperationException exception) {
            exception.printStackTrace();
        }

        final boolean hasSignedMessage = renderSignedMessage != null;

        event.renderer(($, $$, $$$, recipient) -> {
            final var recipientUUID = recipient.get(Identity.UUID);
            final Audience recipientViewer;

            if (recipientUUID.isPresent()) {
                final Audience cached = this.carbonChat.userManager().user(recipientUUID.get()).getNow(null);
                recipientViewer = cached != null ? cached : recipient;
            } else {
                recipientViewer = recipient;
            }

            final Component rendered = chatEvent.renderFor(recipientViewer);
            if (hasSignedMessage) {
                return rendered;
            }

            return rendered;
        });
    }

    /**
     * Clean up per-player state when a player disconnects.
     *
     * <p>The three maps are populated in onPaperChatDecorate and consumed in
     * onPaperChat.  If a player disconnects between those two events (edge case)
     * or if a disconnect occurs before the chat pipeline completes, the entries
     * must be removed to prevent memory leaks.</p>
     */
    @EventHandler
    public void onPlayerQuit(final @NonNull PlayerQuitEvent event) {
        final UUID playerId = event.getPlayer().getUniqueId();
        this.pendingSenders.remove(playerId);
        this.quickPrefixChannels.remove(playerId);
        this.usedQuickPrefix.remove(playerId);
    }

}

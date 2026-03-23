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
import net.draycia.carbon.common.chat.MessageContextSnapshot;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonChatEventImpl;
import net.draycia.carbon.common.event.events.CarbonEarlyChatEvent;
import net.draycia.carbon.common.listeners.ChatListenerInternal;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
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
    private final UserManagerInternal<?> userManager;
    final ConfigManager configManager;
    // Stores an immutable snapshot of all state captured at decoration time
    // (LOWEST priority).  Using a single map eliminates partial-state races
    // where one of the old three maps (pendingSenders / quickPrefixChannels /
    // usedQuickPrefix) could be present while another was missing, and ensures
    // the rendering phase (HIGHEST priority) always sees a consistent,
    // atomic view of the sender, channel, and quick-prefix flag regardless of
    // any permission updates, cache invalidations, or network changes that
    // occur between the two events.  This is the primary fix for the
    // "Checksum mismatch on last seen update" Paper disconnect.
    private final Map<UUID, MessageContextSnapshot> pendingContexts = new ConcurrentHashMap<>();

    @Inject
    public PaperChatListener(
        final CarbonChat carbonChat,
        final UserManagerInternal<?> userManager,
        final CarbonMessages carbonMessages,
        final ConfigManager configManager
    ) {
        super(carbonChat.eventHandler(), carbonMessages, configManager);
        this.carbonChat = carbonChat;
        this.userManager = userManager;
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
        final @Nullable CarbonPlayer sender = this.userManager.cachedUser(playerId);
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
        final boolean prefixUsed = channelMessage.message() != event.originalMessage();

        // Freeze sender reference, channel key, and quick-prefix flag into an
        // immutable snapshot.  Any permission update, cache invalidation, or
        // network change that fires after this point cannot affect the values
        // used by the rendering phase (onPaperChat at HIGHEST priority).
        this.pendingContexts.put(playerId, new MessageContextSnapshot(sender, channel.key(), prefixUsed));
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPaperCommandDecorate(final @NonNull AsyncChatCommandDecorateEvent event) {
        this.onPaperChatDecorate(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPaperChat(final @NonNull AsyncChatEvent event) {
        final UUID playerId = event.getPlayer().getUniqueId();

        // Retrieve the immutable snapshot captured at decoration time (LOWEST).
        // Falling back to a live cache peek is kept for the rare case where
        // decoration was skipped (user not yet loaded at decorate time).
        final @Nullable MessageContextSnapshot snapshot = this.pendingContexts.remove(playerId);
        final CarbonPlayer sender;
        final Key channelKey;
        final boolean prefixUsed;
        if (snapshot != null) {
            sender = snapshot.sender();
            channelKey = snapshot.channelKey();
            prefixUsed = snapshot.usedQuickPrefix();
        } else {
            final @Nullable CarbonPlayer lookedUpSender = this.userManager.cachedUser(playerId);
            if (lookedUpSender == null) {
                event.setCancelled(true);
                return;
            }
            sender = lookedUpSender;
            channelKey = this.carbonChat.channelRegistry().defaultChannel().key();
            prefixUsed = false;
        }

        if (event.viewers().isEmpty()) {
            return;
        }

        final ChatChannel channel = this.carbonChat.channelRegistry().channelOrDefault(channelKey);
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
            final Audience recipientViewer = recipient.get(Identity.UUID)
                .map(this.userManager::cachedUser)
                .map(Audience.class::cast)
                .orElse(recipient);

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
     * <p>The snapshot map is populated in onPaperChatDecorate and consumed in
     * onPaperChat.  If a player disconnects between those two events (edge case)
     * or if a disconnect occurs before the chat pipeline completes, the entry
     * must be removed to prevent memory leaks.</p>
     */
    @EventHandler
    public void onPlayerQuit(final @NonNull PlayerQuitEvent event) {
        this.pendingContexts.remove(event.getPlayer().getUniqueId());
    }

}

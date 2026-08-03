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
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonChatEventImpl;
import net.draycia.carbon.common.event.events.CarbonEarlyChatEvent;
import net.draycia.carbon.common.listeners.ChatListenerInternal;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class PaperChatListener extends ChatListenerInternal implements Listener {

    private final CarbonChat carbonChat;
    final ConfigManager configManager;

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

        final @Nullable CarbonPlayer sender = this.carbonChat.userManager().user(event.player().getUniqueId()).join();
        final @Nullable CarbonEarlyChatEvent earlyChatEvent = this.prepareAndEmitPreChatEvent(sender, event.result());

        if (earlyChatEvent == null || earlyChatEvent.cancelled()) {
            event.setCancelled(true);
            return;
        }

        final @Nullable Component message = this.parseTags(sender, earlyChatEvent.message());

        if (message != null) {
            event.result(message);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPaperCommandDecorate(final @NonNull AsyncChatCommandDecorateEvent event) {
        this.onPaperChatDecorate(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPaperChat(final @NonNull AsyncChatEvent event) {
        final @Nullable CarbonPlayer sender = this.carbonChat.userManager().user(event.getPlayer().getUniqueId()).join();

        if (event.viewers().isEmpty()) {
            return;
        }

        final @Nullable CarbonChatEventImpl chatEvent = this.prepareAndEmitChatEvent(sender, event.message(), event.signedMessage());

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

        event.renderer(($, $$, $$$, recipient) -> {
            final var recipientUUID = recipient.get(Identity.UUID);
            final Audience recipientViewer;

            if (recipientUUID.isPresent()) {
                recipientViewer = this.carbonChat.userManager().user(recipientUUID.get()).join();
            } else {
                recipientViewer = recipient;
            }

            return chatEvent.renderFor(recipientViewer);
        });
    }

}

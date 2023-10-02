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
package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonChatEventImpl;
import net.draycia.carbon.common.listeners.ChatListenerInternal;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.sponge.CarbonChatSponge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.util.Tristate;

@DefaultQualifier(NonNull.class)
public final class SpongeChatListener extends ChatListenerInternal {

    private final CarbonChatSponge carbonChat;

    @Inject
    private SpongeChatListener(
        final CarbonChat carbonChat,
        final CarbonMessages carbonMessages,
        final ConfigManager configManager
    ) {
        super(carbonChat.eventHandler(), carbonMessages, configManager);
        this.carbonChat = (CarbonChatSponge) carbonChat;
    }

    @Listener
    @IsCancelled(Tristate.FALSE)
    public void onPlayerChat(final PlayerChatEvent.Submit event, final @First Player source, final @First SignedMessage signedMessage) {
        final Optional<ServerPlayer> optionalPlayer = event.player();

        if (optionalPlayer.isEmpty()) {
            return;
        }

        final @Nullable CarbonPlayer sender = this.carbonChat.userManager().user(optionalPlayer.get().uniqueId()).join();


        final String content = PlainTextComponentSerializer.plainText().serialize(event.message());
        final @Nullable CarbonChatEventImpl chatEvent = this.prepareAndEmitChatEvent(sender, content, signedMessage);

        if (chatEvent == null || chatEvent.cancelled()) {
            event.setCancelled(true);
            return;
        }

        final List<UUID> recipientIDs = chatEvent.recipients().stream().map(audience ->
            audience.get(Identity.UUID).orElse(new UUID(0,0))).toList();

        event.setFilter(serverPlayer -> recipientIDs.contains(serverPlayer.uniqueId()));

        // No way to render per player, or get the message signature?
        // Why do things keep vanishing with no easily findable alternative? :(
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

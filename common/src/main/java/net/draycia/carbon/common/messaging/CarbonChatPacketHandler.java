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
package net.draycia.carbon.common.messaging;

import java.util.ArrayList;
import java.util.List;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.event.events.CarbonChatEventImpl;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.draycia.carbon.common.messaging.packets.SaveCompletedPacket;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import ninja.egg82.messenger.handler.AbstractMessagingHandler;
import ninja.egg82.messenger.packets.Packet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public final class CarbonChatPacketHandler extends AbstractMessagingHandler {

    private final CarbonChat carbonChat;
    private final UserManagerInternal<?> userManager;

    CarbonChatPacketHandler(
        final CarbonChat carbonChat,
        final MessagingManager messagingManager,
        final UserManagerInternal<?> userManager
    ) {
        super(messagingManager.requirePacketService());
        this.carbonChat = carbonChat;
        this.userManager = userManager;
    }

    @Override
    protected boolean handlePacket(final @NotNull Packet packet) {
        if (packet instanceof SaveCompletedPacket statePacket) {
            this.userManager.saveCompleteMessageReceived(statePacket.playerId());
            return true;
        }

        if (!(packet instanceof ChatMessagePacket messagePacket)) {
            return false;
        }

        final CarbonPlayer sender = this.carbonChat.userManager().user(messagePacket.userId()).join();

        final @Nullable ChatChannel channel = this.carbonChat.channelRegistry().get(messagePacket.channelKey());

        if (channel == null) {
            return false;
        }

        final List<KeyedRenderer> renderers = new ArrayList<>();

        final List<Audience> recipients = channel.recipients(sender);
        final CarbonChatEvent chatEvent = new CarbonChatEventImpl(sender, messagePacket.message(), recipients, renderers, channel, null);
        this.carbonChat.eventHandler().emit(chatEvent);

        for (final Audience recipient : recipients) {
            if (recipient instanceof CarbonPlayer carbonRecipient
                && !carbonRecipient.hasPermission("carbon.crossserver")) {
                continue;
            }

            Component renderedMessage = chatEvent.message();

            for (final KeyedRenderer renderer : chatEvent.renderers()) {
                renderedMessage = renderer.render(sender, recipient, renderedMessage, messagePacket.message());
            }

            recipient.sendMessage(renderedMessage);
        }

        this.carbonChat.server().console().sendMessage(Component.text("[Cross-Server] ").append(chatEvent.message()));

        return true;
    }

}

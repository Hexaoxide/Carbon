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

import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.common.listeners.PingHandler;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.draycia.carbon.common.messaging.packets.SaveCompletedPacket;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.kyori.adventure.text.Component;
import ninja.egg82.messenger.handler.AbstractMessagingHandler;
import ninja.egg82.messenger.packets.Packet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public final class CarbonChatPacketHandler extends AbstractMessagingHandler {

    private final UserManagerInternal<?> userManager;
    private final PingHandler pingHandler;

    CarbonChatPacketHandler(
        final MessagingManager messagingManager,
        final UserManagerInternal<?> userManager,
        final PingHandler pingHandler
    ) {
        super(messagingManager.requirePacketService());
        this.userManager = userManager;
        this.pingHandler = pingHandler;
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

        for (final var recipient : CarbonChatProvider.carbonChat().server().players()) {
            if (recipient.hasPermission(messagePacket.channelPermission() + ".see")) {
                if (recipient.hasPermission("carbon.crossserver")) {
                    if (recipient.ignoring(messagePacket.userId())) {
                        continue;
                    }

                    final Component messageWithPings = this.pingHandler.convertPings(recipient, messagePacket.message());

                    recipient.sendMessage(messageWithPings);
                }
            }
        }

        CarbonChatProvider.carbonChat().server().console().sendMessage(Component.text("[Cross-Server] ").append(messagePacket.message()));

        return true;
    }

}

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
package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.event.events.CarbonChatEventImpl;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.draycia.carbon.common.users.ConsoleCarbonPlayer;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class MessagePacketHandler implements Listener {

    @Inject
    public MessagePacketHandler(
        final CarbonEventHandler events,
        final CarbonChat carbonChat,
        final Provider<MessagingManager> messaging
    ) {
        events.subscribe(CarbonChatEvent.class, 100, false, event -> {
            if (!(event instanceof CarbonChatEventImpl e) || !e.origin) {
                return;
            }
            if (event.sender() instanceof ConsoleCarbonPlayer) {
                return;
            }

            messaging.get().withPacketService(packetService -> {
                final CarbonPlayer sender = event.sender();
                Component networkMessage = event.message();

                for (final var renderer : event.renderers()) {
                    networkMessage = renderer.render(sender, sender, networkMessage, event.originalMessage());
                }

                packetService.queuePacket(new ChatMessagePacket(carbonChat.serverId(), sender.uuid(),
                    event.chatChannel().permission(), event.chatChannel().key(), sender.username(), networkMessage));
            });
        });
    }

}

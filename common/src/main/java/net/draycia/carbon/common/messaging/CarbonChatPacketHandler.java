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
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import ninja.egg82.messenger.handler.AbstractMessagingHandler;
import ninja.egg82.messenger.packets.Packet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public final class CarbonChatPacketHandler extends AbstractMessagingHandler {

    final CarbonChannelRegistry channelRegistry;

    CarbonChatPacketHandler(
        final MessagingManager messagingManager,
        final CarbonChannelRegistry channelRegistry
    ) {
        super(messagingManager.packetService());
        this.channelRegistry = channelRegistry;
    }

    @Override
    protected boolean handlePacket(final @NotNull Packet packet) {
        if (!(packet instanceof ChatMessagePacket messagePacket)) {
            this.logger.info("Messaging packet received - Not a ChatMessagePacket.");
            return false;
        }

        this.logger.info("Messaging packet received");

        final TagResolver.Builder tagResolver = TagResolver.builder();

        for (final var entry : messagePacket.placeholders().entrySet()) {
            tagResolver.tag(entry.getKey(), Tag.inserting(Component.text(entry.getValue())));
        }

        final var component = MiniMessage.miniMessage().deserialize(messagePacket.intermediary(),
            tagResolver.build());

        for (final var recipient : CarbonChatProvider.carbonChat().server().players()) {
            if (recipient.hasPermission(messagePacket.channelPermission() + ".see")) {
                if (recipient.hasPermission("carbon.crossserver")) {
                    recipient.sendMessage(component);
                }
            }
        }

        return true;
    }

}

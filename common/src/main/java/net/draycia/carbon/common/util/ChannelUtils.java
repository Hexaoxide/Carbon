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
package net.draycia.carbon.common.util;

import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ChannelUtils {

    private ChannelUtils() {

    }

    public static void broadcastMessageToChannel(final String mmFormattedMessage, final ChatChannel channel) {
        final Component originalMessage = MiniMessage.miniMessage().deserialize(mmFormattedMessage);

        // TODO: Emit events

        for (final CarbonPlayer recipient : CarbonChatProvider.carbonChat().server().players()) {
            if (channel.hearingPermitted(recipient).permitted()) {
                recipient.sendMessage(originalMessage, MessageType.CHAT);
            }
        }
    }

    public static @Nullable ChatChannel locateChannel(final String channelName) {
        for (final ChatChannel channel : CarbonChatProvider.carbonChat().channelRegistry()) {
            if (channel.commandName().equalsIgnoreCase(channelName)) {
                return channel;
            }
        }

        return null;
    }

}

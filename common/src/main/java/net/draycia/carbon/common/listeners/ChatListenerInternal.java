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

import java.util.ArrayList;
import java.util.List;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public abstract class ChatListenerInternal {

    private final CarbonMessages carbonMessages;
    private final CarbonChat carbonChat;

    protected ChatListenerInternal(
        final CarbonChat carbonChat,
        final CarbonMessages carbonMessages
    ) {
        this.carbonMessages = carbonMessages;
        this.carbonChat = carbonChat;
    }

    protected CarbonChatEvent prepareAndEmitChatEvent(final CarbonPlayer sender, final String messageContent, final SignedMessage signedMessage) {
        Component message = ConfigChatChannel.parseMessageTags(sender, messageContent);

        final CarbonPlayer.ChannelMessage channelMessage = sender.channelForMessage(message);
        final ChatChannel channel = channelMessage.channel();

        message = channelMessage.message();

        if (sender.leftChannels().contains(channel.key())) {
            sender.joinChannel(channel);
            this.carbonMessages.channelJoined(sender);
        }

        final List<KeyedRenderer> renderers = new ArrayList<>();
        renderers.add(KeyedRenderer.keyedRenderer(Key.key("carbon", "default"), channel));

        final List<Audience> recipients = channel.recipients(sender);

        final CarbonChatEvent chatEvent = new CarbonChatEvent(sender, message, recipients, renderers, channel, signedMessage);

        this.carbonChat.eventHandler().emit(chatEvent);

        return chatEvent;
    }

}

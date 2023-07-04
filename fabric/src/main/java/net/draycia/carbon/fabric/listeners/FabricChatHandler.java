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
package net.draycia.carbon.fabric.listeners;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class FabricChatHandler implements ServerMessageEvents.AllowChatMessage {

    private final ConfigFactory configFactory;
    private final CarbonChatFabric carbonChat;
    private final CarbonMessages carbonMessages;

    @Inject
    public FabricChatHandler(
        final ConfigFactory configFactory,
        final CarbonChatFabric carbonChat,
        final CarbonMessages carbonMessages
    ) {
        this.configFactory = configFactory;
        this.carbonChat = carbonChat;
        this.carbonMessages = carbonMessages;
    }

    @Override
    public boolean allowChatMessage(final PlayerChatMessage chatMessage, final ServerPlayer serverPlayer, final ChatType.Bound bound) {
        if (serverPlayer == null) {
            return false;
        }

        final @Nullable CarbonPlayer sender = this.carbonChat.userManager().user(serverPlayer.getUUID()).join();

        String content = chatMessage.decoratedContent().getString();

        for (final Map.Entry<String, String> placeholder : this.configFactory.primaryConfig().chatPlaceholders().entrySet()) {
            content = content.replace(placeholder.getKey(), placeholder.getValue());
        }

        Component message = ConfigChatChannel.parseMessageTags(sender, content);

        final CarbonPlayer.ChannelMessage channelMessage = sender.channelForMessage(message);
        final ChatChannel channel = channelMessage.channel();

        message = channelMessage.message();

        if (sender.leftChannels().contains(channel.key())) {
            sender.joinChannel(channel);
            this.carbonMessages.channelJoined(sender);
        }

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(KeyedRenderer.keyedRenderer(Key.key("carbon", "default"), channel));

        final var recipients = channel.recipients(sender);
        final var chatEvent = new CarbonChatEvent(sender, message, recipients, renderers, channel, chatMessage);
        this.carbonChat.eventHandler().emit(chatEvent);

        if (chatEvent.cancelled()) {
            return false;
        }

        for (final var recipient : chatEvent.recipients()) {
            var renderedMessage = chatEvent.message();

            for (final var renderer : chatEvent.renderers()) {
                renderedMessage = renderer.render(sender, recipient, renderedMessage, chatEvent.message());
            }

            final Component finishedMessage = renderedMessage;

            final net.minecraft.network.chat.Component nativeMessage = FabricAudiences.nonWrappingSerializer().serialize(finishedMessage);
            final PlayerChatMessage customChatMessage = new PlayerChatMessage(chatMessage.link(), chatMessage.signature(), chatMessage.signedBody(), nativeMessage, FilterMask.FULLY_FILTERED);
            final ChatType.Bound customBound = ChatType.bind(CarbonChatFabric.CHAT_TYPE, serverPlayer.level().registryAccess(), nativeMessage);

            if (recipient instanceof CommandSourceStack recipientSource) {
                recipientSource.sendChatMessage(new OutgoingChatMessage.Player(customChatMessage), false, customBound);
            } else if (recipient instanceof CarbonPlayerFabric carbonPlayerFabric) {
                carbonPlayerFabric.player().ifPresent(fabricPlayer -> {
                    fabricPlayer.sendChatMessage(new OutgoingChatMessage.Player(customChatMessage), false, customBound);
                });
            }
        }

        return false;
    }

}

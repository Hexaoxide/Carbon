package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import java.util.Map;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.messaging.CarbonChatPacketHandler;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ninja.egg82.messenger.services.PacketService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class MessagingHandler {

    @Inject
    public MessagingHandler(final CarbonChat carbonChat, final CarbonEventHandler events) {
        events.subscribe(CarbonChatEvent.class, 1000, false, event -> {
            final @Nullable PacketService packetService = carbonChat.packetService();

            if (packetService != null) {
                if (event.chatChannel() instanceof ConfigChatChannel configChatChannel) {
                    final @Nullable String format = configChatChannel.messageFormat(event.sender());

                    packetService.queuePacket(new ChatMessagePacket(carbonChat.serverId(), event.sender().uuid(),
                        configChatChannel.permission(), event.chatChannel().key(), event.sender().username(), format,
                        Map.of("username", event.sender().username(), "message", MiniMessage.miniMessage().serialize(event.message()))));
                }
            }
        });
    }

}

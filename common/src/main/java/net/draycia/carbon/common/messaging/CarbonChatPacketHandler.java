package net.draycia.carbon.common.messaging;

import java.util.ArrayList;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;
import net.kyori.adventure.text.minimessage.placeholder.PlaceholderResolver;
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
    protected boolean handlePacket(@NotNull Packet packet) {
        if (!(packet instanceof ChatMessagePacket messagePacket)) {
            return false;
        }

        final var placeholders = new ArrayList<Placeholder<?>>();

        for (final var entry : messagePacket.getPlaceholders().entrySet()) {
            placeholders.add(Placeholder.miniMessage(entry.getKey(), entry.getValue()));
        }

        final var component = MiniMessage.miniMessage().deserialize(messagePacket.intermediary(),
            PlaceholderResolver.placeholders(placeholders));

        for (final var recipient : CarbonChatProvider.carbonChat().server().players()) {
            if (recipient.hasPermission(messagePacket.channelPermission() + ".see")) {
                if (recipient.hasPermission("carbon.crossserver")) {
                    recipient.sendMessage(Identity.identity(messagePacket.userId()), component);
                }
            }
        }

        return true;
    }

}

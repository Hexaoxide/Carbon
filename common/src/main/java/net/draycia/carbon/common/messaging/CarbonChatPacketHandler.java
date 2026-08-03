/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.command.commands.WhisperCommand;
import net.draycia.carbon.common.event.events.CarbonChatEventImpl;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.draycia.carbon.common.messaging.packets.DisbandPartyPacket;
import net.draycia.carbon.common.messaging.packets.InvalidatePartyInvitePacket;
import net.draycia.carbon.common.messaging.packets.LocalPlayerChangePacket;
import net.draycia.carbon.common.messaging.packets.LocalPlayersPacket;
import net.draycia.carbon.common.messaging.packets.PartyChangePacket;
import net.draycia.carbon.common.messaging.packets.PartyInvitePacket;
import net.draycia.carbon.common.messaging.packets.SaveCompletedPacket;
import net.draycia.carbon.common.messaging.packets.WhisperPacket;
import net.draycia.carbon.common.users.ConsoleCarbonPlayer;
import net.draycia.carbon.common.users.NetworkUsers;
import net.draycia.carbon.common.users.PartyInvites;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import ninja.egg82.messenger.handler.AbstractMessagingHandler;
import ninja.egg82.messenger.packets.Packet;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class CarbonChatPacketHandler extends AbstractMessagingHandler {

    private final CarbonEventHandler events;
    private final CarbonServer server;
    private final ChannelRegistry channels;
    private final UserManagerInternal<?> userManager;
    private final NetworkUsers networkUsers;
    private final WhisperCommand.WhisperHandler whisper;
    private final PartyInvites partyInvites;

    CarbonChatPacketHandler(
        final CarbonChat carbonChat,
        final MessagingManager messagingManager,
        final UserManagerInternal<?> userManager,
        final NetworkUsers networkUsers,
        final WhisperCommand.WhisperHandler whisper,
        final PartyInvites partyInvites
    ) {
        super(messagingManager.requirePacketService());
        this.events = carbonChat.eventHandler();
        this.server = carbonChat.server();
        this.channels = carbonChat.channelRegistry();
        this.userManager = userManager;
        this.networkUsers = networkUsers;
        this.whisper = whisper;
        this.partyInvites = partyInvites;
    }

    @Override
    protected boolean handlePacket(final Packet packet) {
        if (packet instanceof SaveCompletedPacket statePacket) {
            this.userManager.saveCompleteMessageReceived(statePacket.playerId());
            return true;
        } else if (packet instanceof PartyChangePacket pkt) {
            this.userManager.partyChangeMessageReceived(pkt);
            return true;
        } else if (packet instanceof PartyInvitePacket pkt) {
            this.partyInvites.handle(pkt);
            return true;
        } else if (packet instanceof InvalidatePartyInvitePacket pkt) {
            this.partyInvites.handle(pkt);
            return true;
        } else if (packet instanceof DisbandPartyPacket pkt) {
            this.userManager.disbandPartyMessageReceived(pkt);
            return true;
        } else if (packet instanceof ChatMessagePacket messagePacket) {
            this.handleMessagePacket(messagePacket);
            return true; // Don't log an error when the channel doesn't exist
        } else if (packet instanceof LocalPlayersPacket playersPacket) {
            this.networkUsers.handlePacket(playersPacket);
            return true;
        } else if (packet instanceof LocalPlayerChangePacket playerChangePacket) {
            this.networkUsers.handlePacket(playerChangePacket);
            return true;
        } else if (packet instanceof WhisperPacket whisperPacket) {
            this.whisper.handlePacket(whisperPacket);
            return true;
        }

        return false;
    }

    private boolean handleMessagePacket(final ChatMessagePacket messagePacket) {
        final CarbonPlayer sender = this.userManager.user(messagePacket.userId()).join();

        final @Nullable ChatChannel channel = this.channels.channel(messagePacket.channelKey());

        if (channel == null) {
            return false;
        }

        if (!channel.shouldCrossServer()) {
            return false;
        }

        final List<KeyedRenderer> renderers = new ArrayList<>();

        final List<Audience> recipients = channel.recipients(sender);
        final CarbonChatEventImpl chatEvent = new CarbonChatEventImpl(sender, messagePacket.message(), recipients, renderers, channel, null, false);
        this.events.emit(chatEvent);

        renderers.add(KeyedRenderer.keyedRenderer(Key.key("carbon", "console_cs"), ($, recipient, message, original) -> {
            if (recipient instanceof ConsoleCarbonPlayer) {
                return Component.textOfChildren(Component.text("[Cross-Server] "), message);
            }

            return message;
        }));

        for (final Audience recipient : recipients) {
            if (recipient instanceof CarbonPlayer carbonRecipient
                && !carbonRecipient.hasPermission("carbon.crossserver")) {
                continue;
            }

            recipient.sendMessage(chatEvent.renderFor(recipient));
        }

        return true;
    }

}

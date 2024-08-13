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
package net.draycia.carbon.common.command.commands;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonPrivateChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.RawChat;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.ParserFactory;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerParser;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonPrivateChatEventImpl;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messages.SourcedAudience;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.messaging.packets.WhisperPacket;
import net.draycia.carbon.common.users.NetworkUsers;
import net.draycia.carbon.common.util.CloudUtils;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.signed.SignedString;

import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.minecraft.signed.SignedGreedyStringParser.signedGreedyStringParser;

@DefaultQualifier(NonNull.class)
public final class WhisperCommand extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;
    private final ParserFactory parserFactory;
    private final WhisperHandler whisper;

    @Inject
    public WhisperCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final ParserFactory parserFactory,
        final WhisperHandler whisper
    ) {
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.parserFactory = parserFactory;
        this.whisper = whisper;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("whisper", "w", "message", "msg", "m", "tell");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "whisper");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .required("player", this.parserFactory.carbonPlayer(), richDescription(this.carbonMessages.commandWhisperArgumentPlayer()))
            .required("message", signedGreedyStringParser(), richDescription(this.carbonMessages.commandWhisperArgumentMessage()))
            .permission("carbon.whisper.message")
            .senderType(PlayerCommander.class)
            .commandDescription(richDescription(this.carbonMessages.commandWhisperDescription()))
            .handler(ctx -> {
                final CarbonPlayer sender = ctx.sender().carbonPlayer();

                if (sender.muted()) {
                    this.carbonMessages.muteCannotSpeak(sender);
                    return;
                }

                final SignedString message = ctx.get("message");
                final CarbonPlayer recipient = ctx.get("player");

                this.whisper.whisper(sender, recipient, message, ctx.parsingContext("player").consumedInput());
            })
            .build();

        this.commandManager.command(command);
    }

    public static final class WhisperHandler {

        private final Logger logger;
        private final CarbonMessages messages;
        private final ConfigManager configManager;
        private final Provider<MessagingManager> messaging;
        private final PacketFactory packetFactory;
        private final UserManager<? extends CarbonPlayer> userManager;
        private final CarbonServer server;
        private final CarbonEventHandler events;
        private final NetworkUsers network;
        private final Key rawChatKey;

        @Inject
        private WhisperHandler(
            final Logger logger,
            final CarbonMessages messages,
            final ConfigManager configManager,
            final Provider<MessagingManager> messaging,
            final PacketFactory packetFactory,
            final UserManager<?> userManager,
            final CarbonServer server,
            final CarbonEventHandler events,
            final NetworkUsers network,
            @RawChat final Key rawChatKey
        ) {
            this.logger = logger;
            this.messages = messages;
            this.configManager = configManager;
            this.messaging = messaging;
            this.packetFactory = packetFactory;
            this.userManager = userManager;
            this.server = server;
            this.events = events;
            this.network = network;
            this.rawChatKey = rawChatKey;
        }

        public void whisper(
            final CarbonPlayer sender,
            final CarbonPlayer recipient,
            final SignedString message
        ) {
            this.whisper(sender, recipient, message, null);
        }

        public void whisper(
            final CarbonPlayer sender,
            final CarbonPlayer recipient,
            final SignedString message,
            final @Nullable String recipientInputString
        ) {
            if (sender.equals(recipient)) {
                this.messages.whisperSelfError(sender, sender.displayName());
                return;
            }

            if (sender.ignoringDirectMessages() && !sender.hasPermission("carbon.togglemsg.exempt")) {
                this.messages.whisperIgnoringAll(sender);
                return;
            }

            if (!sender.hasPermission("carbon.whisper.send")) {
                this.messages.whisperNoPermissionSend(sender);
                return;
            }

            final String recipientUsername = recipient.username();
            if (!this.network.online(recipient) || !sender.awareOf(recipient) && !sender.hasPermission("carbon.whisper.vanished")) {
                final var exception = new CarbonPlayerParser.ParseException(
                    recipientInputString == null ? recipientUsername : recipientInputString,
                    this.messages
                );
                this.messages.errorCommandArgumentParsing(sender, CloudUtils.message(exception));
                return;
            }

            final boolean localRecipient = recipient.online();

            if (sender.ignoring(recipient)) {
                this.messages.whisperIgnoringTarget(sender, recipient.displayName());
                return;
            }

            if (recipient.ignoring(sender)) {
                this.messages.whisperTargetIgnoring(sender, recipient.displayName());
                return;
            }

            if (recipient.ignoringDirectMessages() && !sender.hasPermission("carbon.togglemsg.exempt")) {
                this.messages.whisperTargetIgnoringDMs(sender, recipient.displayName());
                return;
            }

            final Component senderDisplayName = sender.displayName();
            final Component recipientDisplayName = recipient.displayName();

            final CarbonPrivateChatEvent privateChatEvent = new CarbonPrivateChatEventImpl(sender, recipient, Component.text(message.string()));
            this.events.emit(privateChatEvent);

            if (privateChatEvent.cancelled()) {
                this.messages.whisperError(sender, sender.displayName(), recipient.displayName());
                return;
            }

            final String senderUsername = sender.username();
            message.sendMessage(
                sender,
                ChatType.chatType(this.rawChatKey),
                this.messages.whisperSender(SourcedAudience.of(sender, sender), senderUsername, senderDisplayName, recipientUsername, recipientDisplayName, privateChatEvent.message())
            );
            if (localRecipient) {
                if (!recipient.hasPermission("carbon.whisper.receive")) {
                    this.messages.whisperNoPermissionReceive(sender);
                    return;
                }

                message.sendMessage(
                    recipient,
                    ChatType.chatType(this.rawChatKey),
                    this.messages.whisperRecipient(SourcedAudience.of(sender, recipient), senderUsername, senderDisplayName, recipientUsername, recipientDisplayName, privateChatEvent.message())
                );
            }
            WhisperCommand.broadcastWhisperSpy(this.server, this.messages, senderUsername, senderDisplayName,
                recipientUsername, recipientDisplayName, privateChatEvent.message());
            this.messages.whisperConsoleLog(this.server.console(), senderUsername, senderDisplayName,
                recipientUsername, recipientDisplayName, privateChatEvent.message());

            final @Nullable Sound messageSound = this.configManager.primaryConfig().messageSound();
            if (localRecipient && messageSound != null) {
                recipient.playSound(messageSound);
            }

            sender.lastWhisperTarget(recipient.uuid());
            sender.whisperReplyTarget(recipient.uuid());
            if (localRecipient) {
                recipient.whisperReplyTarget(sender.uuid());
            } else {
                this.messaging.get().queuePacket(() -> this.packetFactory.whisperPacket(sender.uuid(), recipient.uuid(), privateChatEvent.message()));
            }
        }

        public void handlePacket(final WhisperPacket packet) {
            final @Nullable CarbonPlayer recipient = this.server.players().stream()
                .filter(p -> p.uuid().equals(packet.to()))
                .findFirst()
                .orElse(null);
            if (recipient == null) {
                return;
            }
            this.userManager.user(packet.from()).thenAccept(sender -> {
                final String senderUsername = sender.username();
                final Component senderDisplayName = sender.displayName();
                final String recipientUsername = recipient.username();
                final Component recipientDisplayName = recipient.displayName();

                if (!recipient.hasPermission("carbon.whisper.receive")) {
                    this.messages.whisperNoPermissionReceive(sender);
                    return;
                }

                recipient.whisperReplyTarget(sender.uuid());
                SourcedAudience.of(sender, recipient).sendMessage(
                    this.messages.whisperRecipient(SourcedAudience.of(sender, recipient), senderUsername, senderDisplayName, recipientUsername, recipientDisplayName, packet.message())
                );
                WhisperCommand.broadcastWhisperSpy(this.server, this.messages, senderUsername, senderDisplayName, recipientUsername, recipientDisplayName, packet.message());
                this.messages.whisperConsoleLog(this.server.console(), senderUsername, senderDisplayName, recipientUsername, recipientDisplayName, packet.message());
                final @Nullable Sound messageSound = this.configManager.primaryConfig().messageSound();
                if (messageSound != null) {
                    recipient.playSound(messageSound);
                }
            }).exceptionally(ex -> {
                this.logger.warn("Failed to handle whisper packet {}", packet, ex);
                return null;
            });
        }
    }

    public static void broadcastWhisperSpy(
        final CarbonServer server,
        final CarbonMessages messages,
        final String senderUsername,
        final Component senderDisplayName,
        final String recipientUsername,
        final Component recipientDisplayName,
        final Component message
    ) {
        for (final CarbonPlayer player : server.players()) {
            if (player.spying() && !player.username().equals(senderUsername) && !player.username().equals(recipientUsername)) {
                messages.whisperRecipientSpy(player, senderUsername,
                    senderDisplayName, recipientUsername, recipientDisplayName, message);
            }
        }
    }

}

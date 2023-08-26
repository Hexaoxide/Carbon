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
package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.minecraft.extras.RichDescription;
import com.google.inject.Inject;
import com.google.inject.Provider;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonPrivateChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.ArgumentFactory;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonPrivateChatEventImpl;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messages.SourcedAudience;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.messaging.packets.WhisperPacket;
import net.draycia.carbon.common.users.NetworkUsers;
import net.draycia.carbon.common.util.CloudUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class WhisperCommand extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;
    private final ArgumentFactory argumentFactory;
    private final WhisperHandler whisper;

    @Inject
    public WhisperCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final ArgumentFactory argumentFactory,
        final WhisperHandler whisper
    ) {
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.argumentFactory = argumentFactory;
        this.whisper = whisper;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("whisper", "w", "message", "msg", "m", "tell");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "whisper");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(this.argumentFactory.carbonPlayer("player"),
                RichDescription.of(this.carbonMessages.commandWhisperArgumentPlayer()))
            .argument(StringArgument.greedy("message"),
                RichDescription.of(this.carbonMessages.commandWhisperArgumentMessage()))
            .permission("carbon.whisper.message")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandWhisperDescription())
            .handler(ctx -> {
                final CarbonPlayer sender = ((PlayerCommander) ctx.getSender()).carbonPlayer();

                if (sender.muted()) {
                    this.carbonMessages.muteCannotSpeak(sender);
                    return;
                }

                final String message = ctx.get("message");
                final CarbonPlayer recipient = ctx.get("player");

                this.whisper.whisper(sender, recipient, message, ctx.getOrDefault(CarbonPlayerArgument.Parser.INPUT_STRING, null));
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
            final NetworkUsers network
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
        }

        public void whisper(
            final CarbonPlayer sender,
            final CarbonPlayer recipient,
            final String message
        ) {
            this.whisper(sender, recipient, message, null);
        }

        public void whisper(
            final CarbonPlayer sender,
            final CarbonPlayer recipient,
            final String message,
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

            if (!this.network.online(recipient) || !sender.awareOf(recipient) && !sender.hasPermission("carbon.whisper.vanished")) {
                final var exception = new CarbonPlayerArgument.ParseException(
                    recipientInputString == null ? recipient.username() : recipientInputString,
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

            final Component senderName = sender.displayName();
            final Component recipientName = recipient.displayName();

            final CarbonPrivateChatEvent privateChatEvent = new CarbonPrivateChatEventImpl(sender, recipient, Component.text(message));
            this.events.emit(privateChatEvent);

            if (privateChatEvent.cancelled()) {
                this.messages.whisperError(sender, sender.displayName(), recipient.displayName());
                return;
            }

            this.messages.whisperSender(SourcedAudience.of(sender, sender), senderName, recipientName, privateChatEvent.message());
            if (localRecipient) {
                this.messages.whisperRecipient(SourcedAudience.of(sender, recipient), senderName, recipientName, privateChatEvent.message());
            }
            this.messages.whisperConsoleLog(this.server.console(), senderName, recipientName, privateChatEvent.message());

            final @Nullable Sound messageSound = this.configManager.primaryConfig().messageSound();
            if (localRecipient && messageSound != null) {
                recipient.playSound(messageSound);
            }

            sender.lastWhisperTarget(recipient.uuid());
            sender.whisperReplyTarget(recipient.uuid());
            if (localRecipient) {
                recipient.whisperReplyTarget(sender.uuid());
            } else {
                this.messaging.get().withPacketService(packetService -> {
                    packetService.queuePacket(this.packetFactory.whisperPacket(sender.uuid(), recipient.uuid(), privateChatEvent.message()));
                });
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
                final Component senderName = sender.displayName();
                final Component recipientName = recipient.displayName();

                recipient.whisperReplyTarget(sender.uuid());
                this.messages.whisperRecipient(SourcedAudience.of(sender, recipient), senderName, recipientName, packet.message());
                this.messages.whisperConsoleLog(this.server.console(), senderName, recipientName, packet.message());
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

}

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
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.event.events.CarbonPrivateChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.command.ArgumentFactory;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.util.CloudUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class WhisperCommand extends CarbonCommand {

    final CarbonChat carbonChat;
    final CommandManager<Commander> commandManager;
    final CarbonMessages carbonMessages;
    private final ArgumentFactory argumentFactory;
    final ConfigFactory configFactory;

    @Inject
    public WhisperCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final ArgumentFactory argumentFactory,
        final ConfigFactory configFactory
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.argumentFactory = argumentFactory;
        this.configFactory = configFactory;
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
            .argument(this.argumentFactory.carbonPlayer("player").asRequired(),
                RichDescription.of(this.carbonMessages.commandWhisperArgumentPlayer()))
            .argument(StringArgument.greedy("message"),
                RichDescription.of(this.carbonMessages.commandWhisperArgumentMessage()))
            .permission("carbon.whisper.message")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandWhisperDescription())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                if (sender.muted()) {
                    this.carbonMessages.muteCannotSpeak(sender);
                    return;
                }

                final String message = handler.get("message");
                final CarbonPlayer recipient = handler.get("player");

                if (sender.equals(recipient)) {
                    this.carbonMessages.whisperSelfError(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!recipient.online() || !sender.awareOf(recipient) && !sender.hasPermission("carbon.whisper.vanished")) {
                    final var rawNameInput = CloudUtils.rawInputByMatchingName(handler.getRawInput(), recipient);
                    final var exception = new CarbonPlayerArgument.CarbonPlayerParseException(rawNameInput, handler);

                    this.carbonMessages.errorCommandArgumentParsing(sender, CloudUtils.message(exception));
                    return;
                }

                if (sender.ignoring(recipient)) {
                    this.carbonMessages.whisperIgnoringTarget(sender, CarbonPlayer.renderName(recipient));
                    return;
                }

                if (recipient.ignoring(sender)) {
                    this.carbonMessages.whisperTargetIgnoring(sender, CarbonPlayer.renderName(recipient));
                    return;
                }

                final Component senderName = CarbonPlayer.renderName(sender);
                final Component recipientName = CarbonPlayer.renderName(recipient);

                final CarbonPrivateChatEvent privateChatEvent = new CarbonPrivateChatEvent(sender, recipient, Component.text(message));
                this.carbonChat.eventHandler().emit(privateChatEvent);

                if (privateChatEvent.cancelled()) {
                    this.carbonMessages.whisperError(sender, CarbonPlayer.renderName(sender), CarbonPlayer.renderName(recipient));
                    return;
                }

                this.carbonMessages.whisperSender(new SourcedAudience(sender, sender), senderName, recipientName, privateChatEvent.message());
                this.carbonMessages.whisperRecipient(new SourcedAudience(sender, recipient), senderName, recipientName, privateChatEvent.message());
                this.carbonMessages.whisperConsoleLog(this.carbonChat.server().console(), senderName, recipientName, privateChatEvent.message());

                final @Nullable Sound messageSound = this.configFactory.primaryConfig().messageSound();
                if (messageSound != null) {
                    recipient.playSound(messageSound);
                }

                sender.lastWhisperTarget(recipient.uuid());
                sender.whisperReplyTarget(recipient.uuid());
                recipient.whisperReplyTarget(sender.uuid());
            })
            .build();

        this.commandManager.command(command);
    }

}

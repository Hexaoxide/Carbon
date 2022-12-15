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
package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.minecraft.extras.RichDescription;
import com.google.inject.Inject;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.SourcedAudience;
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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ReplyCommand extends CarbonCommand {

    final CarbonChat carbonChat;
    final CommandManager<Commander> commandManager;
    final CarbonMessages carbonMessages;
    final ConfigFactory configFactory;

    @Inject
    public ReplyCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final ConfigFactory configFactory
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.configFactory = configFactory;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("reply", "r");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "reply");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(StringArgument.greedy("message"),
                RichDescription.of(this.carbonMessages.commandReplyArgumentMessage().component()))
            .permission("carbon.whisper.reply")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandReplyDescription().component())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                if (sender.muted()) {
                    this.carbonMessages.muteCannotSpeak(sender);
                    return;
                }

                final String message = handler.get("message");
                final @Nullable UUID replyTarget = sender.whisperReplyTarget();

                if (replyTarget == null) {
                    this.carbonMessages.replyTargetNotSet(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                final ComponentPlayerResult<? extends CarbonPlayer> result = this.carbonChat.server().userManager().carbonPlayer(replyTarget).join();
                final @MonotonicNonNull CarbonPlayer recipient = result.player();

                if (sender.equals(recipient)) {
                    this.carbonMessages.whisperSelfError(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!recipient.online() || !sender.awareOf(recipient) && !sender.hasPermission("carbon.whisper.vanished")) {
                    final var rawNameInput = CloudUtils.rawInputByMatchingName(handler.getRawInput(), recipient);
                    final var exception = new CarbonPlayerArgument.CarbonPlayerParseException(rawNameInput, handler, this.carbonMessages);

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

                this.carbonMessages.whisperSender(new SourcedAudience(sender, sender), senderName, recipientName, message);
                this.carbonMessages.whisperRecipient(new SourcedAudience(sender, recipient), senderName, recipientName, message);
                this.carbonMessages.whisperConsoleLog(this.carbonChat.server().console(), senderName, recipientName, message);

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

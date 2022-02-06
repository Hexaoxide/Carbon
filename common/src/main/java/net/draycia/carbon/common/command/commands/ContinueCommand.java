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
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.util.CloudUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ContinueCommand extends CarbonCommand {

    final CarbonChat carbonChat;
    final CommandManager<Commander> commandManager;
    final CarbonMessageService messageService;

    @Inject
    public ContinueCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.messageService = messageService;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("continue", "c");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "continue");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(StringArgument.greedy("message"),
                RichDescription.of(this.messageService.commandContinueArgumentMessage().component()))
            .permission("carbon.whisper.continue")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messageService.commandContinueDescription().component())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                if (sender.muted()) {
                    this.messageService.muteCannotSpeak(sender);
                    return;
                }

                final String message = handler.get("message");
                final UUID whisperTarget = sender.lastWhisperTarget();

                if (whisperTarget == null) {
                    this.messageService.whisperTargetNotSet(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                final ComponentPlayerResult<? extends CarbonPlayer> result = this.carbonChat.server()
                    .userManager().carbonPlayer(whisperTarget).join();
                final @MonotonicNonNull CarbonPlayer recipient = result.player();

                if (sender.equals(recipient)) {
                    this.messageService.whisperSelfError(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!recipient.online() || !sender.awareOf(recipient) && !sender.hasPermission("carbon.whisper.vanished")) {
                    final var rawNameInput = CloudUtils.rawInputByMatchingName(handler.getRawInput(), recipient);
                    final var exception = new CarbonPlayerArgument.CarbonPlayerParseException(rawNameInput, handler, this.messageService);

                    this.messageService.errorCommandArgumentParsing(sender, CloudUtils.message(exception));
                    return;
                }

                if (sender.ignoring(recipient)) {
                    this.messageService.whisperIgnoringTarget(sender, CarbonPlayer.renderName(recipient));
                    return;
                }

                if (recipient.ignoring(sender)) {
                    this.messageService.whisperTargetIgnoring(sender, CarbonPlayer.renderName(recipient));
                    return;
                }

                final Component senderName = CarbonPlayer.renderName(sender);
                final Component recipientName = CarbonPlayer.renderName(recipient);

                this.messageService.whisperSender(new SourcedAudience(sender, sender), senderName, recipientName, message);
                this.messageService.whisperRecipient(new SourcedAudience(sender, recipient), senderName, recipientName, message);
                this.messageService.whisperConsoleLog(this.carbonChat.server().console(), senderName, recipientName, message);

                sender.lastWhisperTarget(recipient.uuid());
                sender.whisperReplyTarget(recipient.uuid());
                recipient.whisperReplyTarget(sender.uuid());
            })
            .build();

        this.commandManager.command(command);
    }

}

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
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.util.CloudUtils;
import net.kyori.adventure.text.Component;

public class WhisperCommand {

    @Inject
    public WhisperCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonServer carbonServer
    ) {
        final var command = commandManager.commandBuilder("whisper", "w", "message", "msg")
            .argument(CarbonPlayerArgument.newBuilder("player").withMessageService(messageService).asRequired(),
                RichDescription.of(messageService.commandWhisperArgumentPlayer().component()))
            .argument(StringArgument.greedy("message"),
                RichDescription.of(messageService.commandWhisperArgumentMessage().component()))
            .permission("carbon.whisper.message")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, messageService.commandWhisperDescription().component())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                if (sender.muted()) {
                    messageService.muteCannotSpeak(sender);
                    return;
                }

                final String message = handler.get("message");
                final CarbonPlayer recipient = handler.get("player");

                if (sender.equals(recipient)) {
                    messageService.whisperSelfError(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!recipient.online()
                    || (!sender.awareOf(recipient)
                    && !sender.hasPermission("carbon.whisper.vanished"))
                ) {
                    final var rawNameInput = CloudUtils.rawInputByMatchingName(handler.getRawInput(), recipient);
                    final var exception = new CarbonPlayerArgument.CarbonPlayerParseException(rawNameInput, handler, messageService);

                    messageService.errorCommandArgumentParsing(sender, CloudUtils.message(exception));
                    return;
                }

                if (sender.ignoring(recipient)) {
                    messageService.whisperIgnoringTarget(sender, CarbonPlayer.renderName(recipient));
                    return;
                }

                if (recipient.ignoring(sender)) {
                    messageService.whisperTargetIgnoring(sender, CarbonPlayer.renderName(recipient));
                    return;
                }

                final Component senderName = CarbonPlayer.renderName(sender);
                final Component recipientName = CarbonPlayer.renderName(recipient);

                messageService.whisperSender(new SourcedAudience(sender, sender), senderName, recipientName, message);
                messageService.whisperRecipient(new SourcedAudience(sender, recipient), senderName, recipientName, message);
                messageService.whisperConsoleLog(carbonServer.console(), senderName, recipientName, message);

                sender.lastWhisperTarget(recipient.uuid());
                sender.whisperReplyTarget(recipient.uuid());
                recipient.whisperReplyTarget(sender.uuid());
            }) // TODO: let command name and aliases be configurable, because why not
            .build();

        commandManager.command(command);
    }

}

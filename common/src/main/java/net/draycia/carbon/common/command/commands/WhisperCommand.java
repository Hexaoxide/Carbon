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
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.util.CloudUtils;
import net.kyori.adventure.text.Component;

public class WhisperCommand {

    @Inject
    public WhisperCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final CarbonServer carbonServer,
        final PlayerSuggestions suggestionsParser
    ) {
        final var command = commandManager.commandBuilder("whisper", "w", "message", "msg", "m", "tell")
            .argument(CarbonPlayerArgument.newBuilder("player").withMessages(carbonMessages).withSuggestionsProvider(suggestionsParser).withSuggestionsProvider(suggestionsParser).asRequired(),
                RichDescription.of(carbonMessages.commandWhisperArgumentPlayer().component()))
            .argument(StringArgument.greedy("message"),
                RichDescription.of(carbonMessages.commandWhisperArgumentMessage().component()))
            .permission("carbon.whisper.message")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, carbonMessages.commandWhisperDescription().component())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                if (sender.muted()) {
                    carbonMessages.muteCannotSpeak(sender);
                    return;
                }

                final String message = handler.get("message");
                final CarbonPlayer recipient = handler.get("player");

                if (sender.equals(recipient)) {
                    carbonMessages.whisperSelfError(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!recipient.online()
                    || (!sender.awareOf(recipient)
                    && !sender.hasPermission("carbon.whisper.vanished"))
                ) {
                    final var rawNameInput = CloudUtils.rawInputByMatchingName(handler.getRawInput(), recipient);
                    final var exception = new CarbonPlayerArgument.CarbonPlayerParseException(rawNameInput, handler, carbonMessages);

                    carbonMessages.errorCommandArgumentParsing(sender, CloudUtils.message(exception));
                    return;
                }

                if (sender.ignoring(recipient)) {
                    carbonMessages.whisperIgnoringTarget(sender, CarbonPlayer.renderName(recipient));
                    return;
                }

                if (recipient.ignoring(sender)) {
                    carbonMessages.whisperTargetIgnoring(sender, CarbonPlayer.renderName(recipient));
                    return;
                }

                final Component senderName = CarbonPlayer.renderName(sender);
                final Component recipientName = CarbonPlayer.renderName(recipient);

                carbonMessages.whisperSender(new SourcedAudience(sender, sender), senderName, recipientName, message);
                carbonMessages.whisperRecipient(new SourcedAudience(sender, recipient), senderName, recipientName, message);
                carbonMessages.whisperConsoleLog(carbonServer.console(), senderName, recipientName, message);

                sender.lastWhisperTarget(recipient.uuid());
                sender.whisperReplyTarget(recipient.uuid());
                recipient.whisperReplyTarget(sender.uuid());
            }) // TODO: let command name and aliases be configurable, because why not
            .build();

        commandManager.command(command);
    }

}

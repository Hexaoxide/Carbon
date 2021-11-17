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
import com.google.inject.Inject;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.util.CloudUtils;

public class WhisperCommand {

    @Inject
    public WhisperCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonPlayerArgument carbonPlayerArgument
    ) {
        final var command = commandManager.commandBuilder("whisper", "w", "message", "msg")
            .argument(carbonPlayerArgument.newInstance(true, "recipient", CarbonPlayerArgument.NO_SENDER))
            .argument(StringArgument.greedy("message"))
            .permission("carbon.whisper.send") // TODO: carbon.whisper.spy
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                final String message = handler.get("message");
                final CarbonPlayer recipient = handler.get("recipient");

                if (sender.equals(recipient)) {
                    messageService.whisperSelfError(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!recipient.online()
                    || (!sender.awareOf(recipient)
                    && !sender.hasPermission("carbon.seevanish.whisper"))
                ) {
                    final var rawNameInput = CloudUtils.rawInputByMatchingName(handler.getRawInput(), recipient);
                    final var exception = new CarbonPlayerArgument.PlayerParseException(rawNameInput);

                    messageService.errorCommandArgumentParsing(sender, CloudUtils.message(exception));
                    return;
                }

                messageService.whisperSender(new SourcedAudience(sender, sender),
                    CarbonPlayer.renderName(sender), CarbonPlayer.renderName(recipient), message);

                messageService.whisperRecipient(new SourcedAudience(sender, recipient),
                    CarbonPlayer.renderName(sender), CarbonPlayer.renderName(recipient), message);

                sender.lastWhisperTarget(recipient.uuid());
                sender.whisperReplyTarget(recipient.uuid());
                recipient.whisperReplyTarget(sender.uuid());
            }) // TODO: let command name and aliases be configurable, because why not
            .build();

        commandManager.command(command);
    }

}

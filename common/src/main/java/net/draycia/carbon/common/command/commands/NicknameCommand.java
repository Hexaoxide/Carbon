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
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.permission.Permission;
import com.google.inject.Inject;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.command.argument.OptionValueParser;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class NicknameCommand {

    @Inject
    public NicknameCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonPlayerArgument carbonPlayerArgument
    ) {
        final var nicknameArgument = FlagArgument.<Commander, String>ofType(String.class, "value")
            .withParser(new OptionValueParser<>())
            .asOptional()
            .build();

        final var command = commandManager.commandBuilder("nickname", "nick")
            .flag(commandManager.flagBuilder("player")
                .withAliases("p")
                .withArgument(carbonPlayerArgument.newInstance(true, "recipient"))
                .withPermission(Permission.of("carbon.nickname.others"))
            )
            .flag(commandManager.flagBuilder("nickname")
                .withAliases("n")
                .withArgument(nicknameArgument)
                .withPermission(Permission.of("carbon.nickname.set"))
            )
            .flag(commandManager.flagBuilder("reset")
                .withAliases("r")
                .withPermission(Permission.of("carbon.nickname.reset"))
            )
            .permission("carbon.nickname")
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                if (handler.flags().contains("reset")) {
                    final CarbonPlayer target = handler.flags().contains("player") ?
                        handler.flags().get("player") : sender;

                    target.displayName(null);

                    messageService.nicknameReset(target);

                    if (sender != target) {
                        messageService.nicknameResetOthers(sender, target.username());
                    }

                    return;
                }

                // Setting nickname
                if (handler.flags().contains("nickname")) {
                    final var nickname = MiniMessage.miniMessage().parse(handler.flags().get("nickname"));

                    final @MonotonicNonNull CarbonPlayer target = handler.flags().get("player");

                    // Setting other player's nickname
                    if (target != null && !target.equals(sender)) {
                        target.displayName(nickname);
                        messageService.nicknameSet(target, nickname);
                        messageService.nicknameSetOthers(sender, target.username(), nickname);
                    } else {
                        // Setting own nickname
                        sender.displayName(nickname);
                        messageService.nicknameSet(sender, nickname);
                    }
                } else if (handler.flags().contains("player")) {
                    // Checking other player's nickname
                    final CarbonPlayer target = handler.flags().get("player");

                    if (target.displayName() != null) {
                        messageService.nicknameShowOthers(sender, target.username(), target.displayName());
                    } else {
                        messageService.nicknameShowOthersUnset(sender, target.username());
                    }
                } else {
                    // Checking own nickname
                    if (sender.displayName() != null) {
                        messageService.nicknameShow(sender, sender.username(), sender.displayName());
                    } else {
                        messageService.nicknameShowUnset(sender, sender.username());
                    }
                }
            })
            .build();

        commandManager.command(command);
    }

}

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
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.text.Component;

public class ClearChatCommand {

    @Inject
    public ClearChatCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final PrimaryConfig config,
        final CarbonMessageService messageService
    ) {
        final var command = commandManager.commandBuilder("clearchat", "chatclear", "cc")
            .permission("carbon.clearchat.clear")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, messageService.commandClearChatDescription().component())
            .handler(handler -> {
                // Not fond of having to send 50 messages to each player
                // Are we not able to just paste in 50 newlines and call it a day?
                for (int i = 0; i < config.clearChatSettings().iterations(); i++) {
                    for (final var player : carbonChat.server().players()) {
                        if (!player.hasPermission("carbon.clearchat.exempt")) {
                            player.sendMessage(config.clearChatSettings().message());
                        }
                    }
                }

                final Component senderName;

                if (handler.getSender() instanceof PlayerCommander player) {
                    senderName = CarbonPlayer.renderName(player.carbonPlayer());
                } else {
                    senderName = Component.text("Console");
                }

                carbonChat.server().sendMessage(config.clearChatSettings().broadcast(senderName));
            })
            .build();

        commandManager.command(command);
    }

}

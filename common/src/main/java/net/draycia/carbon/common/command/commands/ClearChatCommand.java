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
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.text.Component;

public class ClearChatCommand {

    @Inject
    public ClearChatCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final ConfigFactory configFactory,
        final CarbonMessages carbonMessages
    ) {
        final var command = commandManager.commandBuilder("clearchat", "chatclear", "cc")
            .permission("carbon.clearchat.clear")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, carbonMessages.commandClearChatDescription().component())
            .handler(handler -> {
                // Not fond of having to send 50 messages to each player
                // Are we not able to just paste in 50 newlines and call it a day?
                for (int i = 0; i < configFactory.primaryConfig().clearChatSettings().iterations(); i++) {
                    for (final var player : carbonChat.server().players()) {
                        if (!player.hasPermission("carbon.clearchat.exempt")) {
                            player.sendMessage(configFactory.primaryConfig().clearChatSettings().message());
                        }
                    }
                }

                final Component senderName;
                final String username;

                if (handler.getSender() instanceof PlayerCommander player) {
                    senderName = CarbonPlayer.renderName(player.carbonPlayer());
                    username = player.carbonPlayer().username();
                } else {
                    senderName = Component.text("Console");
                    username = "Console";
                }

                carbonChat.server().sendMessage(configFactory.primaryConfig().clearChatSettings()
                    .broadcast(senderName, username));
            })
            .build();

        commandManager.command(command);
    }

}

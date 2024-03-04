/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ClearChatCommand extends CarbonCommand {

    private final CarbonServer server;
    private final CommandManager<Commander> commandManager;
    private final ConfigManager configManager;
    private final CarbonMessages carbonMessages;

    @Inject
    public ClearChatCommand(
        final CarbonServer server,
        final CommandManager<Commander> commandManager,
        final ConfigManager configManager,
        final CarbonMessages carbonMessages
    ) {
        this.server = server;
        this.commandManager = commandManager;
        this.configManager = configManager;
        this.carbonMessages = carbonMessages;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("clearchat", "chatclear", "cc");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "clearchat");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.clearchat.clear")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandClearChatDescription())
            .handler(handler -> {
                // Not fond of having to send 50 messages to each player
                // Are we not able to just paste in 50 newlines and call it a day?
                for (int i = 0; i < this.configManager.primaryConfig().clearChatSettings().iterations(); i++) {
                    for (final var player : this.server.players()) {
                        if (!player.hasPermission("carbon.clearchat.exempt")) {
                            player.sendMessage(this.configManager.primaryConfig().clearChatSettings().message());
                        }
                    }
                }

                final Component senderName;
                final String username;

                if (handler.getSender() instanceof PlayerCommander player) {
                    senderName = player.carbonPlayer().displayName();
                    username = player.carbonPlayer().username();
                } else {
                    senderName = Component.text("Console");
                    username = "Console";
                }

                this.server.sendMessage(this.configManager.primaryConfig().clearChatSettings()
                    .broadcast(senderName, username));
            })
            .build();

        this.commandManager.command(command);
    }

}

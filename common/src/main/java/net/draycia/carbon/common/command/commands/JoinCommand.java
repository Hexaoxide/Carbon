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
import net.draycia.carbon.api.channels.ChannelPermissionResult;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class JoinCommand extends CarbonCommand {

    private final CarbonChannelRegistry channelRegistry;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;

    @Inject
    public JoinCommand(
        final CarbonChannelRegistry channelRegistry,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        this.channelRegistry = channelRegistry;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("join");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "join");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(StringArgument.<Commander>builder("channel").greedy().withSuggestionsProvider((context, s) -> {
                final CarbonPlayer sender = ((PlayerCommander) context.getSender()).carbonPlayer();
                return sender.leftChannels().stream().map(Key::value).toList();
            }), RichDescription.of(this.carbonMessages.commandJoinDescription()))
            .permission("carbon.join")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandJoinDescription())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final @Nullable ChatChannel channel = this.channelRegistry.channelByValue(handler.get("channel"));
                if (channel == null) {
                    this.carbonMessages.channelNotFound(sender);
                    return;
                }
                final ChannelPermissionResult permitted = channel.speechPermitted(sender);
                if (!permitted.permitted()) {
                    sender.sendMessage(permitted.reason());
                    return;
                }
                if (!sender.leftChannels().contains(channel.key())) {
                    this.carbonMessages.channelNotLeft(sender);
                    return;
                }
                sender.joinChannel(channel);
                this.carbonMessages.channelJoined(sender);

            })
            .build();

        this.commandManager.command(command);
    }

}

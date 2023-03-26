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
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.util.ChannelUtils;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class LeaveCommand extends CarbonCommand {

    final CarbonChat carbonChat;
    final CommandManager<Commander> commandManager;
    final CarbonMessages carbonMessages;

    @Inject
    public LeaveCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("leave");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "leave");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(StringArgument.<Commander>builder("channel").greedy().withSuggestionsProvider((context, s) -> {
                final CarbonPlayer sender = ((PlayerCommander) context.getSender()).carbonPlayer();
                return this.carbonChat.channelRegistry().stream()
                    .filter(x -> !sender.leftChannels().contains(x.key()) && x.speechPermitted(sender).permitted())
                    .map(x -> x.key().value())
                    .toList();
            }), RichDescription.of(this.carbonMessages.commandLeaveDescription()))
            .permission("carbon.join")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandLeaveDescription())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final @Nullable ChatChannel channel = ChannelUtils.locateChannel(handler.get("channel"));
                if (channel == null) {
                    this.carbonMessages.channelNotFound(sender);
                    return;
                }
                if (!channel.speechPermitted(sender).permitted()) {
                    this.carbonMessages.channelNoPermission(sender);
                    return;
                }
                if (sender.leftChannels().contains(channel.key())) {
                    this.carbonMessages.channelAlreadyLeft(sender);
                    return;
                }
                sender.leaveChannel(channel);
                this.carbonMessages.channelLeft(sender);
            })
            .build();

        this.commandManager.command(command);
    }

}

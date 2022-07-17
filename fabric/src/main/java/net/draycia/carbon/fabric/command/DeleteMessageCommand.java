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
package net.draycia.carbon.fabric.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import com.google.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.kyori.adventure.key.Key;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class DeleteMessageCommand extends CarbonCommand {

    final CarbonChatFabric carbonChat;
    final CommandManager<Commander> commandManager;
    final CarbonMessages messageService;
    final PlayerSuggestions playerSuggestions;

    @Inject
    public DeleteMessageCommand(
        final CarbonChatFabric carbonChat,
        final CommandManager<Commander> commandManager,
        final CarbonMessages messageService,
        final PlayerSuggestions playerSuggestions
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.messageService = messageService;
        this.playerSuggestions = playerSuggestions;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("deletemessage");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "deletemessage");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(UUIDArgument.<Commander>newBuilder("messageid").withSuggestionsProvider((sender, input) -> {
                final List<String> messageIds = CarbonChatFabric.messageIdSuggestions();

                if (input.isEmpty()) {
                    return messageIds;
                }

                final List<String> suggestions = new LinkedList<>();

                for (final String messageId : messageIds) {
                    if (messageId.startsWith(input)) {
                        suggestions.add(messageId);
                    }
                }

                return suggestions;
            }).build())
            .permission("carbon.updateusername")
            .senderType(Commander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messageService.commandUpdateUsernameDescription().component())
            .handler(handler -> {
                final @Nullable MessageSignature messageSignature = CarbonChatFabric.messageSignature(handler.get("messageid"));

                if (messageSignature != null) {
                    for (final ServerPlayer player : this.carbonChat.minecraftServer().getPlayerList().getPlayers()) {
                        player.connection.send(new ClientboundDeleteChatPacket(messageSignature));
                    }
                }
            })
            .build();

        this.commandManager.command(command);
    }

}

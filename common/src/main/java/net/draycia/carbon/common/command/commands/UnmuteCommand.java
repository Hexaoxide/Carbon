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
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.minecraft.extras.RichDescription;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.messages.CarbonMessageService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class UnmuteCommand {

    @Inject
    public UnmuteCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonChat carbonChat,
        final PlayerSuggestions suggestionsParser
    ) {
        final var command = commandManager.commandBuilder("unmute")
            .argument(CarbonPlayerArgument.newBuilder("player").withMessageService(messageService).withSuggestionsProvider(suggestionsParser).asOptional(),
                RichDescription.of(messageService.commandUnmuteArgumentPlayer().component()))
            .flag(commandManager.flagBuilder("uuid")
                .withAliases("u")
                .withDescription(RichDescription.of(messageService.commandUnmuteArgumentUUID().component()))
                .withArgument(UUIDArgument.optional("uuid"))
            )
            .permission("carbon.mute.unmute")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, messageService.commandUnmuteDescription().component())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final CarbonPlayer target;

                if (handler.contains("player")) {
                    target = handler.get("player");
                } else if (handler.flags().contains("uuid")) {
                    final var result = carbonChat.server().player(handler.<UUID>get("uuid")).join();
                    target = Objects.requireNonNull(result.player(), "No player found for UUID.");
                } else {
                    messageService.unmuteNoTarget(sender);
                    // TODO: send command syntax
                    return;
                }

                messageService.unmuteAlertRecipient(target);

                for (final var player : carbonChat.server().players()) {
                    if (!player.equals(sender) && !player.hasPermission("carbon.mute.notify")) {
                        continue;
                    }

                    messageService.unmuteAlertPlayers(player, CarbonPlayer.renderName(target));
                }

                target.muted(false);
            })
            .build();

        commandManager.command(command);
    }

}

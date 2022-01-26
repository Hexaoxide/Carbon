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
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.kyori.adventure.key.Key;
import net.draycia.carbon.common.messages.CarbonMessages;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class MuteCommand extends CarbonCommand {

    final CarbonChat carbonChat;
    final CommandManager<Commander> commandManager;
    final CarbonMessages carbonMessages;
    final PlayerSuggestions playerSuggestions;

    @Inject
    public MuteCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final PlayerSuggestions playerSuggestions
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.playerSuggestions = playerSuggestions;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("mute");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "mute");
    }

    @Override
    public void init() {
        final var command = commandManager.commandBuilder("mute")
            .argument(CarbonPlayerArgument.newBuilder("player").withMessages(this.carbonMessages).withSuggestionsProvider(this.playerSuggestions).asOptional(),
                RichDescription.of(this.carbonMessages.commandMuteArgumentPlayer().component()))
            .flag(commandManager.flagBuilder("uuid")
                .withAliases("u")
                .withDescription(RichDescription.of(this.carbonMessages.commandMuteArgumentUUID().component()))
                .withArgument(UUIDArgument.optional("uuid"))
            )
            .permission("carbon.mute")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandMuteDescription().component())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final CarbonPlayer target;

                if (handler.contains("player")) {
                    target = handler.get("player");
                } else if (handler.flags().contains("uuid")) {
                    final var result = this.carbonChat.server().userManager().carbonPlayer(handler.get("uuid")).join();
                    target = Objects.requireNonNull(result.player(), "No player found for UUID.");
                } else {
                    this.carbonMessages.muteNoTarget(sender);
                    // TODO: send command syntax
                    return;
                }

                if (target.hasPermission("carbon.mute.exempt")) {
                    this.carbonMessages.muteExempt(sender);
                    return;
                }

                this.carbonMessages.muteAlertRecipient(target);

                if (!sender.equals(target)) {
                    this.carbonMessages.muteAlertPlayers(sender, CarbonPlayer.renderName(target));
                }

                for (final var player : this.carbonChat.server().players()) {
                    if (player.equals(target) || player.equals(sender)) {
                        continue;
                    }

                    carbonMessages.muteAlertPlayers(player, CarbonPlayer.renderName(target));
                }

                target.muted(true);
            })
            .build();

        this.commandManager.command(command);
    }

}

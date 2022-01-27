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
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.minecraft.extras.RichDescription;
import cloud.commandframework.permission.Permission;
import com.google.inject.Inject;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.command.argument.OptionValueParser;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class NicknameCommand extends CarbonCommand {

    final CommandManager<Commander> commandManager;
    final CarbonMessageService messageService;
    final PlayerSuggestions playerSuggestions;

    @Inject
    public NicknameCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final PlayerSuggestions playerSuggestions
    ) {
        this.commandManager = commandManager;
        this.messageService = messageService;
        this.playerSuggestions = playerSuggestions;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("nickname", "nick");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "nickname");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            // TODO: Allow UUID input for target player
            .flag(this.commandManager.flagBuilder("player")
                .withAliases("p")
                .withDescription(RichDescription.of(this.messageService.commandNicknameArgumentPlayer().component()))
                .withArgument(CarbonPlayerArgument.newBuilder("player").withMessageService(this.messageService).withSuggestionsProvider(this.playerSuggestions).asOptional())
                .withPermission(Permission.of("carbon.nickname.others"))
            )
            .flag(this.commandManager.flagBuilder("nickname")
                .withAliases("n")
                .withDescription(RichDescription.of(this.messageService.commandNicknameArgumentNickname().component()))
                .withArgument(FlagArgument.<Commander, String>ofType(String.class, "value")
                    .withParser(new OptionValueParser<>())
                    .asOptional()
                    .build())
                .withPermission(Permission.of("carbon.nickname.set"))
            )
            .flag(this.commandManager.flagBuilder("reset")
                .withAliases("r")
                .withDescription(RichDescription.of(this.messageService.commandNicknameArgumentReset().component()))
                .withPermission(Permission.of("carbon.nickname.set"))
            )
            .permission("carbon.nickname")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messageService.commandNicknameDescription().component())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                if (handler.flags().contains("reset")) {
                    final CarbonPlayer target = handler.flags().contains("player") ?
                        handler.flags().get("player") : sender;

                    target.displayName(null);

                    this.messageService.nicknameReset(target);

                    if (sender != target) {
                        this.messageService.nicknameResetOthers(sender, target.username());
                    }

                    return;
                }

                // Setting nickname
                if (handler.flags().contains("nickname")) {
                    final var nickname = MiniMessage.miniMessage().deserialize(handler.flags().get("nickname"));

                    final @MonotonicNonNull CarbonPlayer target = handler.flags().get("player");

                    // Setting other player's nickname
                    if (target != null && !target.equals(sender)) {
                        target.displayName(nickname);
                        this.messageService.nicknameSet(target, nickname);
                        this.messageService.nicknameSetOthers(sender, target.username(), nickname);
                    } else {
                        // Setting own nickname
                        if (!sender.hasPermission("carbon.nickname.self")) {
                            this.messageService.nicknameCannotSetOwn(sender);
                            return;
                        }

                        sender.displayName(nickname);
                        this.messageService.nicknameSet(sender, nickname);
                    }
                } else if (handler.flags().contains("player")) {
                    // Checking other player's nickname
                    final CarbonPlayer target = handler.flags().get("player");

                    if (target.displayName() != null) {
                        this.messageService.nicknameShowOthers(sender, target.username(), target.displayName());
                    } else {
                        this.messageService.nicknameShowOthersUnset(sender, target.username());
                    }
                } else {
                    // Checking own nickname
                    if (!sender.hasPermission("carbon.nickname.self")) {
                        this.messageService.nicknameCannotSeeOwn(sender);
                        return;
                    }

                    if (sender.displayName() != null) {
                        this.messageService.nicknameShow(sender, sender.username(), sender.displayName());
                    } else {
                        this.messageService.nicknameShowUnset(sender, sender.username());
                    }
                }
            })
            .build();

        this.commandManager.command(command);
    }

}

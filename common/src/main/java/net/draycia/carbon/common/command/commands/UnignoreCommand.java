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
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.minecraft.extras.RichDescription;
import com.google.inject.Inject;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.ArgumentFactory;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class UnignoreCommand extends CarbonCommand {

    private final UserManager<?> users;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;
    private final ArgumentFactory argumentFactory;

    @Inject
    public UnignoreCommand(
        final UserManager<?> userManager,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final ArgumentFactory argumentFactory
    ) {
        this.users = userManager;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.argumentFactory = argumentFactory;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("unignore", "unblock");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "unignore");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            // TODO: Filter, and only show muted players, but allow inputting any player name.
            .argument(this.argumentFactory.carbonPlayer("player").asOptional(),
                RichDescription.of(this.carbonMessages.commandUnignoreArgumentPlayer()))
            .flag(this.commandManager.flagBuilder("uuid")
                .withAliases("u")
                .withDescription(RichDescription.of(this.carbonMessages.commandUnignoreArgumentUUID()))
                .withArgument(UUIDArgument.optional("uuid"))
            )
            .permission("carbon.ignore.unignore")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandUnignoreDescription())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final CarbonPlayer target;

                if (handler.contains("player")) {
                    target = handler.get("player");
                } else if (handler.flags().contains("uuid")) {
                    target = this.users.user(handler.get("uuid")).join();
                } else {
                    this.carbonMessages.ignoreTargetInvalid(sender);
                    return;
                }

                if (!sender.ignoring(target)) {
                    this.carbonMessages.notIgnored(sender, target.displayName());
                    return;
                }

                sender.ignoring(target, false);
                this.carbonMessages.noLongerIgnoring(sender, target.displayName());
            })
            .build();

        this.commandManager.command(command);
    }

}

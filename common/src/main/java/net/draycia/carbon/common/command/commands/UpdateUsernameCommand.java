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
import java.util.Objects;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.ArgumentFactory;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class UpdateUsernameCommand extends CarbonCommand {

    private final UserManager<?> userManager;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages messageService;
    private final ArgumentFactory argumentFactory;
    private final ProfileResolver profileResolver;

    @Inject
    public UpdateUsernameCommand(
        final UserManager<?> userManager,
        final CommandManager<Commander> commandManager,
        final CarbonMessages messageService,
        final ArgumentFactory argumentFactory,
        final ProfileResolver profileResolver
    ) {
        this.userManager = userManager;
        this.commandManager = commandManager;
        this.messageService = messageService;
        this.argumentFactory = argumentFactory;
        this.profileResolver = profileResolver;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("updateusername", "updatename");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "updateusername");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(this.argumentFactory.carbonPlayer("player").asOptional(),
                RichDescription.of(this.messageService.commandUpdateUsernameArgumentPlayer()))
            .flag(this.commandManager.flagBuilder("uuid")
                .withAliases("u")
                .withDescription(RichDescription.of(this.messageService.commandUpdateUsernameArgumentUUID()))
                .withArgument(UUIDArgument.optional("uuid"))
            )
            .permission("carbon.updateusername")
            .senderType(Commander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messageService.commandUpdateUsernameDescription())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                CarbonPlayer target;

                if (handler.contains("player")) {
                    target = handler.get("player");
                } else if (handler.flags().contains("uuid")) {
                    target = this.userManager.user(handler.get("uuid")).join();
                } else {
                    target = sender;
                }

                if (target instanceof WrappedCarbonPlayer wrappedPlayer) {
                    target = wrappedPlayer.carbonPlayerCommon();
                } else if (!(target instanceof CarbonPlayerCommon)) {
                    this.messageService.usernameNotUpdated(sender);
                    return;
                }

                this.messageService.usernameFetching(sender);
                final CarbonPlayer finalTarget = target;
                this.profileResolver.resolveName(target.uuid()).thenAccept(name -> {
                    Objects.requireNonNull(name, "Unable to fetch username for player.");

                    ((CarbonPlayerCommon) finalTarget).username(name);
                    this.messageService.usernameUpdated(sender, name);
                });
            })
            .build();

        this.commandManager.command(command);
    }

}

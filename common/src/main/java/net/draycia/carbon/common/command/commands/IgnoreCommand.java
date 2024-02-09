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

import com.google.inject.Inject;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.ParserFactory;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;

import static org.incendo.cloud.description.CommandDescription.commandDescription;
import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.parser.standard.UUIDParser.uuidParser;

@DefaultQualifier(NonNull.class)
public final class IgnoreCommand extends CarbonCommand {

    private final UserManager<?> users;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;
    private final ParserFactory parserFactory;

    @Inject
    public IgnoreCommand(
        final UserManager<?> userManager,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final ParserFactory parserFactory
    ) {
        this.users = userManager;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.parserFactory = parserFactory;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("ignore", "block");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "ignore");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .optional("player", this.parserFactory.carbonPlayer(), richDescription(this.carbonMessages.commandIgnoreArgumentPlayer()))
            .flag(this.commandManager.flagBuilder("uuid")
                .withAliases("u")
                .withDescription(richDescription(this.carbonMessages.commandIgnoreArgumentUUID()))
                .withComponent(uuidParser())
            )
            .permission("carbon.ignore")
            .senderType(PlayerCommander.class)
            .commandDescription(commandDescription(richDescription(this.carbonMessages.commandIgnoreDescription())))
            .handler(handler -> {
                final CarbonPlayer sender = handler.sender().carbonPlayer();
                final CarbonPlayer target;

                if (handler.contains("player")) {
                    target = handler.get("player");
                } else if (handler.flags().contains("uuid")) {
                    target = this.users.user(handler.get("uuid")).join();
                } else {
                    this.carbonMessages.ignoreTargetInvalid(sender);
                    return;
                }

                if (target.hasPermission("carbon.ignore.exempt")) {
                    this.carbonMessages.ignoreExempt(sender, target.displayName());
                    return;
                }

                if (sender.ignoring(target)) {
                    this.carbonMessages.alreadyIgnored(sender, target.displayName());
                    return;
                }

                sender.ignoring(target, true);
                this.carbonMessages.nowIgnoring(sender, target.displayName());
            })
            .build();

        this.commandManager.command(command);
    }

}

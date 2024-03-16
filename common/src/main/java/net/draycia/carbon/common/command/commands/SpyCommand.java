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
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;

import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.parser.standard.BooleanParser.booleanParser;

@DefaultQualifier(NonNull.class)
public final class SpyCommand extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;

    @Inject
    public SpyCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("spy");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "spy");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .optional("enabled", booleanParser())
            .permission("carbon.spy")
            .senderType(PlayerCommander.class)
            .commandDescription(richDescription(this.carbonMessages.commandSpyDescription()))
            .handler(handler -> {
                final CarbonPlayer sender = handler.sender().carbonPlayer();

                boolean enabled = !sender.spying();

                if (handler.contains("enabled")) {
                    enabled = handler.get("enabled");
                }

                sender.spying(enabled);
                if (enabled) {
                    this.carbonMessages.commandSpyEnabled(sender);
                } else {
                    this.carbonMessages.commandSpyDisabled(sender);
                }
            })
            .build();

        this.commandManager.command(command);
    }

}

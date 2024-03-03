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
import org.incendo.cloud.CommandManager;

import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;

public class ToggleMessagesCommand extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;

    @Inject
    public ToggleMessagesCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("togglemsg", "togglepm");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "togglemsg");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.togglemsg")
            .senderType(PlayerCommander.class)
            .commandDescription(richDescription(this.carbonMessages.commandToggleMsgDescription()))
            .handler(handler -> {
                final CarbonPlayer sender = handler.sender().carbonPlayer();
                final boolean nowIgnoring = !sender.ignoringDirectMessages();
                sender.ignoringDirectMessages(nowIgnoring);

                if (nowIgnoring) {
                    this.carbonMessages.whispersToggledOff(sender);
                } else {
                    this.carbonMessages.whispersToggledOn(sender);
                }
            })
            .build();

        this.commandManager.command(command);

        final var toggleOn = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.togglemsg")
            .literal("on", "allow")
            .senderType(PlayerCommander.class)
            .commandDescription(richDescription(this.carbonMessages.commandToggleMsgDescription()))
            .handler(handler -> {
                final CarbonPlayer sender = handler.sender().carbonPlayer();
                sender.ignoringDirectMessages(false);
                this.carbonMessages.whispersToggledOn(sender);
            })
            .build();

        this.commandManager.command(toggleOn);

        final var toggleOff = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.togglemsg")
            .literal("off", "ignore")
            .senderType(PlayerCommander.class)
            .commandDescription(richDescription(this.carbonMessages.commandToggleMsgDescription()))
            .handler(handler -> {
                final CarbonPlayer sender = handler.sender().carbonPlayer();
                sender.ignoringDirectMessages(true);
                this.carbonMessages.whispersToggledOff(sender);
            })
            .build();

        this.commandManager.command(toggleOff);
    }

}

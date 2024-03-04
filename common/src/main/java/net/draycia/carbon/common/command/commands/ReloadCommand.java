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
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import com.google.inject.Inject;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.event.events.CarbonReloadEvent;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ReloadCommand extends CarbonCommand {

    private final CarbonEventHandler events;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;

    @Inject
    public ReloadCommand(
        final CarbonEventHandler eventHandler,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        this.events = eventHandler;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("carbon");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "reload");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .literal("reload")
            .permission("carbon.reload")
            .senderType(Commander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandReloadDescription())
            .handler(handler -> {
                // TODO: Check if all listeners succeeded
                this.events.emit(new CarbonReloadEvent());
                this.carbonMessages.configReloaded(handler.getSender());
            })
            .build();

        this.commandManager.command(command);
    }

}

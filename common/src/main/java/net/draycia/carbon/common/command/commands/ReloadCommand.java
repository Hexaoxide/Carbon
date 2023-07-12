/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
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
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.event.CarbonEventHandlerImpl;
import net.draycia.carbon.common.event.events.CarbonReloadEvent;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ReloadCommand extends CarbonCommand {

    final CarbonChat carbonChat;
    final CommandManager<Commander> commandManager;
    final CarbonMessages carbonMessages;
    private final CarbonEventHandlerImpl events;

    @Inject
    public ReloadCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final CarbonEventHandlerImpl events
    ) {
        this.carbonChat = carbonChat;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.events = events;
    }

    @Override
    protected CommandSettings _commandSettings() {
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
                final var result = this.events.emitWithResult(new CarbonReloadEvent());
                if (result.noErrors()) {
                    this.carbonMessages.configReloaded(handler.getSender());
                } else {
                    this.carbonMessages.configReloadFailed(handler.getSender());
                }
            })
            .build();

        this.commandManager.command(command);
    }

}

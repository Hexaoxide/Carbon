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
import com.google.inject.Inject;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.draycia.carbon.common.messages.CarbonMessageService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ReloadCommand {

    @Inject
    public ReloadCommand(
        final CommandManager<Commander> commandManager,
        final ConfigFactory configFactory,
        final CarbonChannelRegistry channelRegistry,
        final CarbonMessageService messageService
    ) {
        final var command = commandManager.commandBuilder("creload", "carbonreload")
            .permission("carbon.reload")
            .senderType(Commander.class)
            .handler(handler -> {
                channelRegistry.reloadRegisteredConfigChannels();

                final @Nullable PrimaryConfig config = configFactory.reloadPrimaryConfig();

                if (config != null) {
                    messageService.configReloaded(handler.getSender());
                } else {
                    messageService.configReloadFailed(handler.getSender());
                }

            })
            .build();

        commandManager.command(command);
    }

}

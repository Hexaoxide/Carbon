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
import java.util.Locale;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;

import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;

@DefaultQualifier(NonNull.class)
public final class RealNameCommand extends CarbonCommand {

    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;
    private final CarbonServer server;

    @Inject
    public RealNameCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final CarbonServer server
    ) {
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.server = server;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("realname", "rn");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "realname");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .required("player", StringParser.greedyStringParser(),
                richDescription(this.carbonMessages.commandRealNameArgumentPlayer()))
            .permission("carbon.realname")
            .senderType(Commander.class)
            .commandDescription(richDescription(this.carbonMessages.commandRealNameDescription()))
            .handler(handler -> {
                final String input = handler.<String>get("player").split(" ")[0].toLowerCase(Locale.ENGLISH);
                boolean found = false;

                for (final CarbonPlayer player : this.server.players()) {
                    if (player.vanished() && !handler.sender().hasPermission("carbon.realname.vanished")) {
                        continue;
                    }

                    final String plainName = PlainTextComponentSerializer.plainText().serialize(player.displayName()).toLowerCase(Locale.ENGLISH);

                    if (plainName.contains(input)) {
                        found = true;
                        this.carbonMessages.realName(handler.sender(), player.displayName(), player.username());
                    }
                }

                if (!found) {
                    this.carbonMessages.realNameTargetInvalid(handler.sender(), input);
                }
            })
            .build();

        this.commandManager.command(command);
    }

}

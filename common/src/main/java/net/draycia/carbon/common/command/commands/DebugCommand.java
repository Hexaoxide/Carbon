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
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.minecraft.extras.RichDescription;
import com.google.inject.Inject;
import java.util.ArrayList;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class DebugCommand extends CarbonCommand {

    final CommandManager<Commander> commandManager;
    final CarbonMessages carbonMessages;
    final PlayerSuggestions playerSuggestions;

    @Inject
    public DebugCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages,
        final PlayerSuggestions playerSuggestions
    ) {
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
        this.playerSuggestions = playerSuggestions;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("carbondebug", "cdebug");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "debug");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .argument(CarbonPlayerArgument.newBuilder("player").withMessages(this.carbonMessages).withSuggestionsProvider(this.playerSuggestions).asOptional(),
                RichDescription.of(this.carbonMessages.commandDebugArgumentPlayer().component()))
            .permission("carbon.debug")
            .senderType(PlayerCommander.class)
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.carbonMessages.commandDebugDescription().component())
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final CarbonPlayer target;

                if (handler.contains("player")) {
                    target = handler.get("player");
                } else {
                    target = sender;
                }

                sender.sendMessage(
                    Component.join(JoinConfiguration.noSeparators(),
                        Component.text("Primary Group: ", NamedTextColor.GOLD),
                        Component.text(target.primaryGroup(), NamedTextColor.GREEN))
                );

                final var groups = new ArrayList<Component>();

                for (final var group : target.groups()) {
                    groups.add(Component.text(group, NamedTextColor.GREEN));
                }

                final var formattedGroupsList =
                    Component.join(JoinConfiguration.separator(
                        Component.text(", ", NamedTextColor.YELLOW)), groups
                    );

                sender.sendMessage(
                    Component.join(JoinConfiguration.noSeparators(),
                        Component.text("Groups: ", NamedTextColor.GOLD),
                        formattedGroupsList
                    )
                );
            })
            .build();

        this.commandManager.command(command);
    }

}

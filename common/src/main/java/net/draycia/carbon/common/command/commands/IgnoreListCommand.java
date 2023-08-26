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
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import com.google.inject.Inject;
import java.util.function.Supplier;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.util.Pagination;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class IgnoreListCommand extends CarbonCommand {

    private final UserManager<?> users;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages messages;

    @Inject
    public IgnoreListCommand(
        final UserManager<?> userManager,
        final CommandManager<Commander> commandManager,
        final CarbonMessages messages
    ) {
        this.users = userManager;
        this.commandManager = commandManager;
        this.messages = messages;
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("ignorelist", "listignores");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "ignorelist");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .permission("carbon.ignore")
            .senderType(PlayerCommander.class)
            .argument(IntegerArgument.<Commander>builder("page").withMin(1).asOptionalWithDefault(1))
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, this.messages.commandIgnoreListDescription())
            .handler(this::execute)
            .build();

        this.commandManager.command(command);
    }

    private void execute(final CommandContext<Commander> ctx) {
        final Pagination<Supplier<CarbonPlayer>> pagination = Pagination.<Supplier<CarbonPlayer>>builder()
            .header((currPage, pages) -> {
                return Component.text("Ignores page " + currPage + "/" + pages);
            })
            .item((e, lastOfPage) -> {
                final CarbonPlayer ignoredPlayer = e.get();
                return Component.text(" - ").append(
                    ignoredPlayer.displayName().hoverEvent(
                        Component.text("Username: " + ignoredPlayer.username() + ", UUID: " + ignoredPlayer.uuid())
                    )
                );
            })
            //.footer() // todo page buttons
            .pageOutOfRange((currPage, pages) -> {
                return Component.text("Page " + currPage + " out of range " + pages);
            })
            .build();

        final CarbonPlayer sender = ((PlayerCommander) ctx.getSender()).carbonPlayer();
        final int page = ctx.get("page");

        pagination.render(
            sender.ignoring().stream()
                .sorted() // this way page numbers make sense
                .map(id -> (Supplier<CarbonPlayer>) () -> this.users.user(id).join())
                .toList(),
            page,
            6
        ).forEach(sender::sendMessage);
    }

}

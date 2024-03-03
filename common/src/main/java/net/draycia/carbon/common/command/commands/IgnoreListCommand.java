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
import java.util.function.Supplier;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.util.Pagination;
import net.draycia.carbon.common.util.PaginationHelper;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.context.CommandContext;

import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;

@DefaultQualifier(NonNull.class)
public final class IgnoreListCommand extends CarbonCommand {

    private final UserManager<?> users;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages messages;
    private final PaginationHelper pagination;

    @Inject
    public IgnoreListCommand(
        final UserManager<?> userManager,
        final CommandManager<Commander> commandManager,
        final CarbonMessages messages,
        final PaginationHelper pagination
    ) {
        this.users = userManager;
        this.commandManager = commandManager;
        this.messages = messages;
        this.pagination = pagination;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
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
            .optional("page", integerParser(1), DefaultValue.constant(1))
            .commandDescription(richDescription(this.messages.commandIgnoreListDescription()))
            .handler(this::execute)
            .build();

        this.commandManager.command(command);
    }

    private void execute(final CommandContext<PlayerCommander> ctx) {
        final CarbonPlayer sender = ctx.sender().carbonPlayer();
        final var elements = sender.ignoring().stream()
            .sorted() // this way page numbers make sense
            .map(id -> (Supplier<CarbonPlayer>) () -> this.users.user(id).join())
            .toList();

        if (elements.isEmpty()) {
            this.messages.commandIgnoreListNoneIgnored(sender);
            return;
        }

        final Pagination<Supplier<CarbonPlayer>> pagination = Pagination.<Supplier<CarbonPlayer>>builder()
            .header(this.messages::commandIgnoreListPaginationHeader)
            .item((e, lastOfPage) -> {
                final CarbonPlayer p = e.get();
                return this.messages.commandIgnoreListPaginationElement(p.displayName(), p.username());
            })
            .footer(this.pagination.footerRenderer(p -> "/" + this.commandSettings().name() + " " + p))
            .pageOutOfRange(this.messages::paginationOutOfRange)
            .build();

        final int page = ctx.get("page");

        pagination.render(elements, page, 6).forEach(sender::sendMessage);
    }

}

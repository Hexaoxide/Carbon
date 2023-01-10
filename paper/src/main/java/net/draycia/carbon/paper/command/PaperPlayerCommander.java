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
package net.draycia.carbon.paper.command;

import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNull;

@DefaultQualifier(NonNull.class)
public record PaperPlayerCommander(
    CarbonServer server,
    Player player
) implements PlayerCommander, PaperCommander {

    @Override
    public CommandSender commandSender() {
        return this.player;
    }

    @Override
    public Audience audience() {
        return this.player;
    }

    @Override
    public CarbonPlayer carbonPlayer() {
        return requireNonNull(this.server.userManager().carbonPlayer(this.player.getUniqueId()).join().player(), "No CarbonPlayer for logged in Player!");
    }

    @Override
    public boolean hasPermission(final String permission) {
        return this.player.hasPermission(permission);
    }

}

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
package net.draycia.carbon.bukkit.command;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

public record BukkitPlayerCommander(
    @NonNull CarbonChat carbon,
    @NonNull Player player
) implements PlayerCommander, BukkitCommander {

    @Override
    public @NonNull CommandSender commandSender() {
        return this.player;
    }

    @Override
    public @NonNull Audience audience() {
        return this.player;
    }

    @Override
    public @NonNull CarbonPlayer carbonPlayer() {
        return requireNonNull(this.carbon.server().player(this.player.getUniqueId()).join().player(), "No CarbonPlayer for logged in Player!");
    }

}

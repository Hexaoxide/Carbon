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
package net.draycia.carbon.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import static java.util.Objects.requireNonNull;

@NullMarked
public record VelocityPlayerCommander(
    UserManager<?> userManager,
    Player player
) implements PlayerCommander, VelocityCommander {

    @Override
    public CommandSource commandSource() {
        return this.player;
    }

    @Override
    public @NonNull Audience audience() {
        return this.player;
    }

    @Override
    public CarbonPlayer carbonPlayer() {
        return requireNonNull(this.userManager.user(this.player.getUniqueId()).join(), "No CarbonPlayer for logged in Player!");
    }

    @Override
    public boolean hasPermission(final String permission) {
        return this.player.hasPermission(permission);
    }

}

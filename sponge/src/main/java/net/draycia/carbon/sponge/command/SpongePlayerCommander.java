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
package net.draycia.carbon.sponge.command;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static java.util.Objects.requireNonNull;

@DefaultQualifier(NonNull.class)
public record SpongePlayerCommander(
    CarbonChat carbon,
    ServerPlayer player,
    CommandCause commandCause
) implements PlayerCommander, SpongeCommander {

    @Override
    public CarbonPlayer carbonPlayer() {
        return requireNonNull(this.carbon.server().player(this.player.uniqueId()).join().player(), "No CarbonPlayer for logged in Player!");
    }

    @Override
    public @NotNull Audience audience() {
        return this.commandCause.audience();
    }

}

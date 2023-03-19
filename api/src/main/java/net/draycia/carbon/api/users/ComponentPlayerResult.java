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
package net.draycia.carbon.api.users;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;

/**
 * The result of a player data operation.
 *
 * @param player the {@link CarbonPlayer}, or null if unsuccessful
 * @param reason the reason of the result, typically empty unless {@link #player} is null
 * @since 2.0.0
 */
@Deprecated(forRemoval = true)
@ApiStatus.Internal
public record ComponentPlayerResult<C extends CarbonPlayer>(@MonotonicNonNull C player, Component reason) {

}

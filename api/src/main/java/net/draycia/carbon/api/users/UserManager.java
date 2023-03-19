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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manager used to load/obtain and save {@link CarbonPlayer CarbonPlayers}.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface UserManager<C extends CarbonPlayer> {

    /**
     * Loads and returns a {@link CarbonPlayer} with the given {@link UUID}.
     *
     * @param uuid the player's uuid
     * @return the result
     * @since 2.0.0
     */
    @Deprecated(forRemoval = true)
    CompletableFuture<ComponentPlayerResult<C>> carbonPlayer(final UUID uuid);

    CompletableFuture<C> user(final UUID uuid);

    /**
     * Saves the {@link CarbonPlayer} and returns the result.
     *
     * @param player the player to save
     * @return the result
     * @since 2.0.0
     */
    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    CompletableFuture<ComponentPlayerResult<C>> savePlayer(final C player);

    /**
     * Saves the {@link CarbonPlayer}, returns the result, and invalidates the player entry.
     *
     * @param player the player to save
     * @return the result
     * @since 2.0.0
     */
    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    CompletableFuture<ComponentPlayerResult<C>> saveAndInvalidatePlayer(final C player);

}

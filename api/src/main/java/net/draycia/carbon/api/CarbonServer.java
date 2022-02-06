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
package net.draycia.carbon.api;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * The server that carbon is running on.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface CarbonServer extends Audience {

    /**
     * The server's console.
     *
     * @return the server's console
     * @since 2.0.0
     */
    Audience console();

    /**
     * The players that are online on the server.
     *
     * @return the online players
     * @since 2.0.0
     */
    List<? extends CarbonPlayer> players();

    /**
     * Manager used to load/obtain and save {@link CarbonPlayer CarbonPlayers}.
     *
     * @return the user manager
     * @since 2.1.0
     */
    UserManager<? extends CarbonPlayer> userManager();

    /**
     * Obtains the desired user's UUID.
     *
     * @param username the user's username
     * @return the user's UUID
     * @since 2.0.0
     */
    CompletableFuture<@Nullable UUID> resolveUUID(final String username);

    /**
     * Obtains the desired player's name.
     *
     * @param uuid the user's UUID
     * @return the user's name
     * @since 2.0.0
     */
    CompletableFuture<@Nullable String> resolveName(final UUID uuid);

}

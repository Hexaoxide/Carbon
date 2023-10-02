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
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Manager used to load/obtain and save {@link CarbonPlayer CarbonPlayers}.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface UserManager<C extends CarbonPlayer> {

    /**
     * Gets the {@link CarbonPlayer} for the provided player {@link UUID}, whether they are online or not.
     *
     * <p>Note that the returned user object/future is <i>not</i> guaranteed to be the same for subsequent calls.</p>
     *
     * <p>Because of this, the return value should <i>not</i> be cached, it should be queried each time it is needed. The implementation handles caching as is appropriate.</p>
     *
     * @param uuid the player's id
     * @return the player
     * @since 2.1.0
     */
    CompletableFuture<C> user(UUID uuid);

    /**
     * Create a new {@link Party} with the specified name.
     *
     * <p>Parties with no users will not be saved. Use {@link Party#disband()} to discard.</p>
     * <p>The returned reference will expire after one minute, store {@link Party#id()} rather than the instance and use {@link #party(UUID)} to retrieve.</p>
     *
     * @param name party name
     * @return new party
     * @since 2.1.0
     */
    Party createParty(Component name);

    /**
     * Look up an existing party by its id.
     *
     * <p>As parties that have never had a user are not saved, they are not retrievable here.</p>
     * <p>The returned reference will expire after one minute, do not cache it. The implementation handles caching as is appropriate.</p>
     *
     * @param id party id
     * @return existing party
     * @see #createParty(Component)
     * @since 2.1.0
     */
    CompletableFuture<@Nullable Party> party(UUID id);

}

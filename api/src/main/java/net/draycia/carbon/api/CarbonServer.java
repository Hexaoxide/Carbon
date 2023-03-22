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
package net.draycia.carbon.api;

import java.util.List;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
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
     * @deprecated Use {@link CarbonChat#userManager} (internal code: inject the UserManager)
     */
    @Deprecated(forRemoval = true)
    UserManager<? extends CarbonPlayer> userManager();

}

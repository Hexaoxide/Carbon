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
package net.draycia.carbon.api.event.events;

import java.util.UUID;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Called when a player is added to a {@link Party}.
 *
 * @since 3.0.0
 */
@DefaultQualifier(NonNull.class)
public interface PartyJoinEvent extends CarbonEvent {

    /**
     * ID of the player joining a party.
     *
     * <p>The player's {@link CarbonPlayer#party()} field is not guaranteed to be updated immediately,
     * especially if the change needs to propagate cross-server.</p>
     *
     * @return player id
     * @since 3.0.0
     */
    UUID playerId();

    /**
     * The party being joined.
     *
     * <p>{@link Party#members()} will reflect the new member.</p>
     *
     * @return party
     * @since 3.0.0
     */
    Party party();

}

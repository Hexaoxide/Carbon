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

import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Reference to a chat party.
 *
 * @see UserManager#createParty(Component)
 * @see UserManager#party(UUID)
 * @since 3.0.0
 */
@DefaultQualifier(NonNull.class)
public interface Party {

    /**
     * Get the name of this party.
     *
     * @return party name
     * @since 3.0.0
     */
    Component name();

    /**
     * Get the unique id of this party.
     *
     * @return party id
     * @since 3.0.0
     */
    UUID id();

    /**
     * Get a snapshot of the current party members.
     *
     * @return party members
     * @since 3.0.0
     */
    Set<UUID> members();

    /**
     * Add a user to this party. They will automatically be removed from their previous party if necessary.
     *
     * @param id user id
     * @since 3.0.0
     */
    void addMember(UUID id);

    /**
     * Remove a user from this party.
     *
     * @param id user id
     * @since 3.0.0
     */
    void removeMember(UUID id);

    /**
     * Disband this party. Will remove all members and delete persistent data.
     *
     * @since 3.0.0
     */
    void disband();

}

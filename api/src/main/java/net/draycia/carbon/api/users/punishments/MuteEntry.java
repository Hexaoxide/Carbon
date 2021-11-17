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
package net.draycia.carbon.api.users.punishments;

import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * An entry describing when and why and how a player was muted.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public record MuteEntry(
    long muteEpoch,
    @Nullable UUID muteCause,
    long expirationEpoch,
    @Nullable String reason,
    @Nullable Key channel,
    UUID muteId
) {

    /**
     * Returns if this entry is still active and should be enforced.
     *
     * @return if this entry is still valid
     * @since 2.0.0
     */
    public boolean valid() {
        return this.expirationEpoch == -1 || System.currentTimeMillis() < this.expirationEpoch;
    }

}

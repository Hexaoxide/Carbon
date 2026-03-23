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
package net.draycia.carbon.paper.messages;

import com.google.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class PlaceholderValueCache {

    private final Map<UUID, Map<String, String>> valuesByPlayer = new ConcurrentHashMap<>();

    public void warm(final UUID playerId) {
        this.valuesByPlayer.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>());
    }

    public void remove(final UUID playerId) {
        this.valuesByPlayer.remove(playerId);
    }

    public void invalidate(final UUID playerId) {
        final @Nullable Map<String, String> playerValues = this.valuesByPlayer.get(playerId);
        if (playerValues != null) {
            playerValues.clear();
        }
    }

    public String placeholder(final Player player, final String token) {
        final Map<String, String> playerValues = this.valuesByPlayer.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>());
        return playerValues.computeIfAbsent(token, ignored -> PlaceholderAPI.setPlaceholders(player, token));
    }

}

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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Singleton;
import java.time.Duration;
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

    private static final Duration ENTRY_TTL = Duration.ofSeconds(30);

    private final Map<UUID, Cache<String, String>> valuesByPlayer = new ConcurrentHashMap<>();

    private static Cache<String, String> newPlayerCache() {
        return Caffeine.newBuilder()
            .expireAfterWrite(ENTRY_TTL)
            .build();
    }

    public void warm(final UUID playerId) {
        this.valuesByPlayer.computeIfAbsent(playerId, ignored -> newPlayerCache());
    }

    public void remove(final UUID playerId) {
        this.valuesByPlayer.remove(playerId);
    }

    public void invalidate(final UUID playerId) {
        final @Nullable Cache<String, String> playerCache = this.valuesByPlayer.get(playerId);
        if (playerCache != null) {
            playerCache.invalidateAll();
        }
    }

    public String placeholder(final Player player, final String token) {
        final Cache<String, String> playerCache = this.valuesByPlayer.computeIfAbsent(
            player.getUniqueId(), ignored -> newPlayerCache());
        final @Nullable String value = playerCache.get(token, t -> PlaceholderAPI.setPlaceholders(player, t));
        return value != null ? value : token;
    }

}

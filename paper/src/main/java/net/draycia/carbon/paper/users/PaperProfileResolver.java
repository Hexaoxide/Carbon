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
package net.draycia.carbon.paper.users;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.util.ConcurrentUtil;
import org.apache.logging.log4j.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

// TODO caching impl using Mojang api. Need to account for rate limits as well
@Singleton
@DefaultQualifier(NonNull.class)
public final class PaperProfileResolver implements ProfileResolver {

    private final Server server;
    private final Logger logger;
    private final ExecutorService executor;

    @Inject
    private PaperProfileResolver(final Server server, final Logger logger) {
        this.server = server;
        this.logger = logger;
        this.executor = Executors.newFixedThreadPool(2, ConcurrentUtil.carbonThreadFactory(logger, "PaperProfileResolver"));
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable Player online = this.server.getPlayer(username);
            if (online != null) {
                return online.getUniqueId();
            }
            final PlayerProfile profile = this.server.createProfile(username);
            profile.complete(true);
            if (profile.isComplete()) {
                return profile.getId();
            }
            return null;
        }, this.executor).exceptionally(thr -> {
            this.logger.warn("Failed to resolve UUID for player with name {}", username, thr);
            return null;
        });
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable Player online = this.server.getPlayer(uuid);
            if (online != null) {
                return online.getName();
            }
            final PlayerProfile profile = this.server.createProfile(uuid);
            profile.complete(true);
            if (profile.isComplete()) {
                return profile.getName();
            }
            return null;
        }, this.executor).exceptionally(thr -> {
            this.logger.warn("Failed to resolve player name for uuid {}", uuid, thr);
            return null;
        });
    }

    @Override
    public void shutdown() {
        ConcurrentUtil.shutdownExecutor(this.executor, TimeUnit.MILLISECONDS, 50);
    }

}

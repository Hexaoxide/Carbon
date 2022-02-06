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
package net.draycia.carbon.bukkit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.users.CarbonPlayerBukkit;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerBukkit implements CarbonServer, ForwardingAudience.Single {

    private final CarbonChatBukkit chatBukkitEntry;
    private final UserManager<CarbonPlayerBukkit> userManager;

    @Inject
    private CarbonServerBukkit(final CarbonChatBukkit chatBukkitEntry, final UserManager<CarbonPlayerCommon> userManager) {
        this.chatBukkitEntry = chatBukkitEntry;
        this.userManager = new BukkitUserManager(userManager);
    }

    @Override
    public Audience audience() {
        return this.chatBukkitEntry.getServer();
    }

    @Override
    public Audience console() {
        return this.chatBukkitEntry.getServer().getConsoleSender();
    }

    @Override
    public List<? extends CarbonPlayer> players() {
        final var players = new ArrayList<CarbonPlayer>();

        for (final var player : this.chatBukkitEntry.getServer().getOnlinePlayers()) {
            final ComponentPlayerResult<CarbonPlayerBukkit> result = this.userManager.carbonPlayer(player.getUniqueId()).join();

            if (result.player() != null) {
                players.add(result.player());
            }
        }

        return players;
    }

    @Override
    public UserManager<CarbonPlayerBukkit> userManager() {
        return this.userManager;
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        // TODO: user cache?
        return CompletableFuture.supplyAsync(() -> {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);

            if (!offlinePlayer.hasPlayedBefore()) {
                return null;
            }

            return offlinePlayer.getUniqueId();
        });
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        // TODO: user cache?
        return CompletableFuture.supplyAsync(() -> {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

            if (!offlinePlayer.hasPlayedBefore()) {
                return null;
            }

            return offlinePlayer.getName();
        });
    }

}

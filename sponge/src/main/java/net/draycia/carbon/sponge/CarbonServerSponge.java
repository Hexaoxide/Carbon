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
package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.sponge.users.CarbonPlayerSponge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.ProfileNotFoundException;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerSponge implements CarbonServer, ForwardingAudience.Single {

    private final Game game;
    private final UserManager<CarbonPlayerSponge> userManager;

    @Inject
    private CarbonServerSponge(final UserManager<CarbonPlayerCommon> userManager, final Game game) {
        this.game = game;
        this.userManager = new SpongeUserManager(userManager);
    }

    @Override
    public @NotNull Audience audience() {
        return this.game.server();
    }

    @Override
    public Audience console() {
        return this.game.systemSubject();
    }

    @Override
    public List<CarbonPlayerSponge> players() {
        final var players = new ArrayList<CarbonPlayerSponge>();

        for (final var player : Sponge.server().onlinePlayers()) {
            final ComponentPlayerResult<CarbonPlayerSponge> result = this.userManager.carbonPlayer(player.uniqueId()).join();

            if (result.player() != null) {
                players.add(result.player());
            }
        }

        return players;
    }

    @Override
    public UserManager<CarbonPlayerSponge> userManager() {
        return this.userManager;
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Sponge.server().gameProfileManager().basicProfile(username).join().uuid();
            } catch (final ProfileNotFoundException exception) {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Sponge.server().gameProfileManager().basicProfile(uuid).join().name().orElse(null);
            } catch (final ProfileNotFoundException exception) {
                return null;
            }
        });
    }

}

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
import java.util.List;
import java.util.Objects;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerSponge implements CarbonServer, ForwardingAudience.Single {

    private final Game game;
    private final UserManager<?> userManager;

    @Inject
    private CarbonServerSponge(final Game game, final UserManagerInternal<?> userManager) {
        this.game = game;
        this.userManager = userManager;
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
    public List<? extends CarbonPlayer> players() {
        return this.game.server().onlinePlayers().stream()
            .map(sponge -> this.userManager.user(sponge.uniqueId()).getNow(null))
            .filter(Objects::nonNull)
            .toList();
    }

}

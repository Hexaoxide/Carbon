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
package net.draycia.carbon.common.util;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.users.UserManagerInternal;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class PlayerUtils {

    private PlayerUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <C extends CarbonPlayer> List<CompletableFuture<Void>> saveLoggedInPlayers(
        final CarbonServer carbonServer,
        final UserManagerInternal<C> userManager,
        final Logger logger
    ) {
        return carbonServer.players().stream()
            .map(player -> PlayerUtils.savePlayer(userManager, (C) player, logger))
            .toList();
    }

    public static <C extends CarbonPlayer> CompletableFuture<Void> savePlayer(
        final UserManagerInternal<C> userManager,
        final C player,
        final Logger logger
    ) {
        final var saveResult = userManager.saveIfNeeded(player);
        saveResult.exceptionally(saveExceptionHandler(logger, player.username(), player.uuid()));
        return saveResult;
    }

    public static <T> Function<Throwable, @Nullable T> joinExceptionHandler(final Logger logger) {
        return thr -> {
            logger.warn("Exception handling player join", thr);
            return null;
        };
    }

    public static Function<Throwable, @Nullable Void> saveExceptionHandler(final Logger logger, final String username, final UUID uuid) {
        return thr -> {
            logger.warn("Exception saving data for player {} with uuid {}", username, uuid);
            return null;
        };
    }

}

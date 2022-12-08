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
package net.draycia.carbon.common.users.db;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Update;

import static net.kyori.adventure.text.Component.empty;

@DefaultQualifier(NonNull.class)
public abstract class AbstractUserManager implements UserManager<CarbonPlayerCommon> {

    protected final Jdbi jdbi;

    protected final QueriesLocator locator;

    protected AbstractUserManager(final Jdbi jdbi, final QueriesLocator locator) {
        this.jdbi = jdbi;
        this.locator = locator;
    }

    @Override
    final public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> savePlayer(final CarbonPlayerCommon player) {
        return CompletableFuture.supplyAsync(() -> this.jdbi.withHandle(handle -> {
            this.bindPlayerArguments(handle.createUpdate(this.locator.query("save-player")), player)
                .execute();

            if (!player.ignoredPlayers().isEmpty()) {
                final PreparedBatch batch = handle.prepareBatch(this.locator.query("save-ignores"));

                for (final UUID ignoredPlayer : player.ignoredPlayers()) {
                    batch.bind("id", player.uuid()).bind("ignoredplayer", ignoredPlayer).add();
                }

                batch.execute();
            }

            // TODO: save ignoredplayers
            return new ComponentPlayerResult<>(player, empty());
        }));
    }

    abstract protected Update bindPlayerArguments(final Update update, final CarbonPlayerCommon player);
}

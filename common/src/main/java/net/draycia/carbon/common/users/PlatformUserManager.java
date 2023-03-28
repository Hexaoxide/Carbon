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
package net.draycia.carbon.common.users;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.messaging.packets.PlayerStatePacket;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class PlatformUserManager<C extends WrappedCarbonPlayer> implements UserManagerInternal<C> {

    private final UserManagerInternal<CarbonPlayerCommon> backingManager;

    public PlatformUserManager(final UserManagerInternal<CarbonPlayerCommon> backingManager) {
        this.backingManager = backingManager;
    }

    @Override
    public CompletableFuture<C> user(final UUID uuid) {
        return this.backingManager.user(uuid).thenApply(common -> {
            final C wrapped = this.wrap(common);
            this.updateTransientLoadedStatus(wrapped);
            return wrapped;
        });
    }

    protected abstract C wrap(final CarbonPlayerCommon common);

    protected abstract void updateTransientLoadedStatus(C wrapped);

    @Override
    public void shutdown() {
        this.backingManager.shutdown();
    }

    @Override
    public void stateMessageReceived(final PlayerStatePacket.Type type, final UUID playerId) {
        this.backingManager.stateMessageReceived(type, playerId);
    }

    @Override
    public CompletableFuture<Void> saveIfNeeded(final C player) {
        return this.backingManager.saveIfNeeded(player.carbonPlayerCommon());
    }

    @Override
    public CompletableFuture<Void> save(final C player) {
        return this.backingManager.save(player.carbonPlayerCommon());
    }

    @Override
    public CompletableFuture<Void> loggedOut(final UUID uuid) {
        return this.backingManager.loggedOut(uuid);
    }

    @Override
    public void cleanup() {
        this.backingManager.cleanup();
    }

}

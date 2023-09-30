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

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.messaging.packets.PartyChangePacket;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class PlatformUserManager implements UserManagerInternal<WrappedCarbonPlayer> {

    private final UserManagerInternal<CarbonPlayerCommon> backingManager;
    private final PlayerFactory playerFactory;

    @Inject
    private PlatformUserManager(
        final @Backing UserManagerInternal<CarbonPlayerCommon> backingManager,
        final PlayerFactory playerFactory
    ) {
        this.backingManager = backingManager;
        this.playerFactory = playerFactory;
    }

    @Override
    public CompletableFuture<WrappedCarbonPlayer> user(final UUID uuid) {
        return this.backingManager.user(uuid).thenApply(common -> {
            final WrappedCarbonPlayer wrapped = this.playerFactory.wrap(common);
            common.markTransientLoaded(!wrapped.online());
            return wrapped;
        });
    }

    @Override
    public void shutdown() {
        this.backingManager.shutdown();
    }

    @Override
    public void saveCompleteMessageReceived(final UUID playerId) {
        this.backingManager.saveCompleteMessageReceived(playerId);
    }

    @Override
    public CompletableFuture<Void> saveIfNeeded(final WrappedCarbonPlayer player) {
        return this.backingManager.saveIfNeeded(player.carbonPlayerCommon());
    }

    @Override
    public CompletableFuture<Void> loggedOut(final UUID uuid) {
        return this.backingManager.loggedOut(uuid);
    }

    @Override
    public void cleanup() {
        this.backingManager.cleanup();
    }

    @Override
    public CompletableFuture<@Nullable PartyImpl> party(final UUID id) {
        return this.backingManager.party(id);
    }

    @Override
    public CompletableFuture<Void> saveParty(final PartyImpl info) {
        return this.backingManager.saveParty(info);
    }

    @Override
    public void disbandParty(final UUID id) {
        this.backingManager.disbandParty(id);
    }

    @Override
    public void partyChangeMessageReceived(final PartyChangePacket pkt) {
        this.backingManager.partyChangeMessageReceived(pkt);
    }

    public interface PlayerFactory {

        WrappedCarbonPlayer wrap(CarbonPlayerCommon common);

        static Module moduleFor(final Class<? extends WrappedCarbonPlayer> carbonPlayerImpl) {
            return new FactoryModuleBuilder()
                .implement(WrappedCarbonPlayer.class, carbonPlayerImpl)
                .build(PlatformUserManager.PlayerFactory.class);
        }

    }

}

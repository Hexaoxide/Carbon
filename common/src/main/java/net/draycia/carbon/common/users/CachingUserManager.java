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

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.messaging.packets.PartyChangePacket;
import net.draycia.carbon.common.users.db.DatabaseUserManager;
import net.draycia.carbon.common.util.ConcurrentUtil;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.common.users.PlayerUtils.saveExceptionHandler;

@DefaultQualifier(NonNull.class)
public abstract class CachingUserManager implements UserManagerInternal<CarbonPlayerCommon> {

    protected final Logger logger;
    protected final ProfileResolver profileResolver;
    private final ExecutorService executor;
    private final MembersInjector<CarbonPlayerCommon> playerInjector;
    private final Provider<MessagingManager> messagingManager;
    private final PacketFactory packetFactory;
    private final ReentrantLock cacheLock;
    private final Map<UUID, CompletableFuture<CarbonPlayerCommon>> cache;
    private final AsyncCache<UUID, PartyImpl> partyCache;

    protected CachingUserManager(
        final Logger logger,
        final ProfileResolver profileResolver,
        final MembersInjector<CarbonPlayerCommon> playerInjector,
        final Provider<MessagingManager> messagingManager,
        final PacketFactory packetFactory
    ) {
        this.logger = logger;
        this.executor = Executors.newSingleThreadExecutor(ConcurrentUtil.carbonThreadFactory(logger, this.getClass().getSimpleName()));
        this.partyCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .buildAsync();
        this.profileResolver = profileResolver;
        this.playerInjector = playerInjector;
        this.messagingManager = messagingManager;
        this.packetFactory = packetFactory;
        this.cacheLock = new ReentrantLock();
        this.cache = new HashMap<>();
    }

    protected abstract CarbonPlayerCommon loadOrCreate(UUID uuid);

    protected abstract void saveSync(CarbonPlayerCommon player);

    protected abstract @Nullable PartyImpl loadParty(UUID uuid);

    protected abstract void saveSync(PartyImpl info, Map<UUID, PartyImpl.ChangeType> polledChanges);

    protected abstract void disbandSync(UUID id);

    private CompletableFuture<Void> save(final CarbonPlayerCommon player) {
        return CompletableFuture.runAsync(() -> {
            this.saveSync(player);
            player.saved();
            this.messagingManager.get().withPacketService(packetService -> {
                packetService.queuePacket(this.packetFactory.saveCompletedPacket(player.uuid()));
                packetService.flushQueue();
            });
        }, this.executor);
    }

    @Override
    public void saveCompleteMessageReceived(final UUID playerId) {
        this.cacheLock.lock();
        try {
            this.cache.remove(playerId);
        } finally {
            this.cacheLock.unlock();
        }
    }

    @Override
    public CompletableFuture<Void> saveIfNeeded(final CarbonPlayerCommon player) {
        if (!player.needsSave()) {
            return CompletableFuture.completedFuture(null);
        }
        return this.save(player);
    }

    @Override
    public CompletableFuture<CarbonPlayerCommon> user(final UUID uuid) {
        this.cacheLock.lock();
        try {
            return this.cache.computeIfAbsent(uuid, $ -> {
                final CompletableFuture<CarbonPlayerCommon> future = CompletableFuture.supplyAsync(() -> {
                    final CarbonPlayerCommon player = this.loadOrCreate(uuid);
                    this.playerInjector.injectMembers(player);
                    if (this instanceof DatabaseUserManager) {
                        player.registerPropertyUpdateListener(() ->
                            this.save(player).exceptionally(saveExceptionHandler(this.logger, player.username, uuid)));
                    }
                    return player;
                }, this.executor);
                this.attachPostLoad(uuid, future);
                return future;
            });
        } finally {
            this.cacheLock.unlock();
        }
    }

    @Override
    public void shutdown() {
        this.cacheLock.lock();
        try {
            final Map<UUID, CompletableFuture<Void>> collect = List.copyOf(this.cache.keySet()).stream()
                .collect(Collectors.toMap(Function.identity(), this::loggedOut));
            for (final Map.Entry<UUID, CompletableFuture<Void>> entry : collect.entrySet()) {
                try {
                    entry.getValue().join();
                } catch (final Exception ex) {
                    this.logger.warn("Exception saving data for player with uuid '{}'", entry.getKey(), ex);
                }
            }
            ConcurrentUtil.shutdownExecutor(this.executor, TimeUnit.MILLISECONDS, 500);
        } finally {
            this.cacheLock.unlock();
        }
    }

    @Override
    public CompletableFuture<Void> loggedOut(final UUID uuid) {
        this.messagingManager.get().withPacketService(packetService -> {
            packetService.queuePacket(this.packetFactory.removeLocalPlayerPacket(uuid));
        });
        this.cacheLock.lock();
        try {
            final @Nullable CompletableFuture<CarbonPlayerCommon> remove = this.cache.remove(uuid);
            if (remove != null && remove.isDone()) { // don't need to save if it never finished loading
                final @Nullable CarbonPlayerCommon join = remove.join();
                if (join != null) {
                    return this.saveIfNeeded(join);
                }
            }
            return CompletableFuture.completedFuture(null);
        } finally {
            this.cacheLock.unlock();
        }
    }

    @Override
    public void cleanup() {
        this.cacheLock.lock();
        try {
            for (final Map.Entry<UUID, CompletableFuture<CarbonPlayerCommon>> entry : Map.copyOf(this.cache).entrySet()) {
                final @Nullable CarbonPlayerCommon getNow = entry.getValue().getNow(null);
                if (getNow == null || !getNow.transientLoadedNeedsUnload()) {
                    continue;
                }
                this.cache.remove(entry.getKey());
                this.saveIfNeeded(getNow).exceptionally(saveExceptionHandler(this.logger, getNow.username, getNow.uuid()));
            }
        } finally {
            this.cacheLock.unlock();
        }
    }

    // Don't keep failed requests, so they can be retried on the next request
    // The caller is expected to handle the error
    private void attachPostLoad(final UUID uuid, final CompletableFuture<CarbonPlayerCommon> future) {
        future.whenComplete((result, thr) -> {
            if (result == null || thr != null) {
                this.cacheLock.lock();
                try {
                    this.cache.remove(uuid);
                } finally {
                    this.cacheLock.unlock();
                }
            }
        });
    }

    @Override
    public CompletableFuture<@Nullable PartyImpl> party(final UUID id) {
        return this.partyCache.get(id, (uuid, cacheExecutor) -> {
            return CompletableFuture.supplyAsync(() -> this.loadParty(uuid), this.executor);
        });
    }

    @Override
    public CompletableFuture<Void> saveParty(final PartyImpl info) {
        return CompletableFuture.runAsync(() -> {
            final Map<UUID, PartyImpl.ChangeType> changes = info.pollChanges();
            if (changes.isEmpty()) {
                return;
            }
            this.saveSync(info, changes);
            this.messagingManager.get().withPacketService(service -> {
                service.queuePacket(this.packetFactory.partyChange(info.id(), changes));
                service.flushQueue();
            });
        }, this.executor);
    }

    @Override
    public final void disbandParty(final UUID id) {
        this.partyCache.synchronous().invalidate(id);
        this.executor.execute(() -> this.disbandSync(id));
    }

    @Override
    public void partyChangeMessageReceived(final PartyChangePacket pkt) {
        final @Nullable CompletableFuture<PartyImpl> future = this.partyCache.getIfPresent(pkt.partyId());
        if (future != null) {
            future.thenAccept(party -> pkt.changes().forEach((id, type) -> {
                switch (type) {
                    case ADD -> party.rawMembers().add(id);
                    case REMOVE -> party.rawMembers().remove(id);
                }
            })).exceptionally(thr -> {
                thr.printStackTrace(); // todo
                return null;
            });
        }
    }
}

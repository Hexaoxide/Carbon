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
package net.draycia.carbon.common.users;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Injector;
import com.google.inject.Provider;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.DisbandPartyPacket;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.messaging.packets.PartyChangePacket;
import net.draycia.carbon.common.users.db.DatabaseUserManager;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.common.users.PlayerUtils.saveExceptionHandler;

@DefaultQualifier(NonNull.class)
public abstract class CachingUserManager implements UserManagerInternal<CarbonPlayerCommon> {

    private static final int DISBAND_DELAY = 10;

    protected final Logger logger;
    protected final ProfileResolver profileResolver;
    private final ExecutorService executor;
    private final Injector injector;
    private final Provider<MessagingManager> messagingManager;
    private final PacketFactory packetFactory;
    private final CarbonServer server;
    private final ReentrantLock cacheLock;
    private final Map<UUID, CompletableFuture<CarbonPlayerCommon>> cache;
    private final AsyncCache<UUID, Party> partyCache;
    private final List<Runnable> queuedDisbands = new CopyOnWriteArrayList<>();
    private final Cache<UUID, Object> recentDisbands = Caffeine.newBuilder()
        .expireAfterWrite(DISBAND_DELAY + 10, TimeUnit.SECONDS)
        .build();

    protected CachingUserManager(
        final Logger logger,
        final ProfileResolver profileResolver,
        final Injector injector,
        final Provider<MessagingManager> messagingManager,
        final PacketFactory packetFactory,
        final CarbonServer server
    ) {
        this.logger = logger;
        this.executor = Executors.newSingleThreadExecutor(ConcurrentUtil.carbonThreadFactory(logger, this.getClass().getSimpleName()));
        this.partyCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .buildAsync();
        this.profileResolver = profileResolver;
        this.injector = injector;
        this.messagingManager = messagingManager;
        this.packetFactory = packetFactory;
        this.server = server;
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
            this.messagingManager.get().queuePacketAndFlush(() -> this.packetFactory.saveCompletedPacket(player.uuid()));
        }, this.executor);
    }

    @Override
    public Party createParty(final Component name) {
        throw new UnsupportedOperationException();
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
                    this.injector.injectMembers(player);
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
        for (final Runnable task : this.queuedDisbands) {
            task.run();
        }
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
        this.messagingManager.get().queuePacket(() -> this.packetFactory.removeLocalPlayerPacket(uuid));
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
    public CompletableFuture<@Nullable Party> party(final UUID id) {
        // we delay party deletion for cross-server purposes, so ignore present data when we know it was recently disbanded
        if (this.recentDisbands.getIfPresent(id) != null) {
            return CompletableFuture.completedFuture(null);
        }
        return this.partyCache.get(id, (uuid, cacheExecutor) -> CompletableFuture.supplyAsync(() -> {
            final @Nullable PartyImpl party = this.loadParty(uuid);
            if (party != null) {
                this.injector.injectMembers(party);
            }
            return party;
        }, this.executor));
    }

    @Override
    public CompletableFuture<Void> saveParty(final PartyImpl info) {
        return CompletableFuture.runAsync(() -> {
            final Map<UUID, PartyImpl.ChangeType> changes = info.pollChanges();
            if (changes.isEmpty()) {
                return;
            }
            this.saveSync(info, changes);
            this.messagingManager.get().queuePacketAndFlush(() -> this.packetFactory.partyChange(info.id(), changes));
        }, this.executor);
    }

    @Override
    public final void disbandParty(final UUID id) {
        this.partyCache.synchronous().invalidate(id);
        final AtomicBoolean ran = new AtomicBoolean(false);
        final AtomicReference<Runnable> taskRef = new AtomicReference<>();
        final Runnable task = () -> {
            if (ran.compareAndSet(false, true)) {
                this.disbandSync(id);
                this.queuedDisbands.remove(taskRef.get());
            }
        };
        taskRef.set(task);
        this.queuedDisbands.add(task);
        this.recentDisbands.put(id, new Object());
        // delay deletion so other servers can post leave events
        CompletableFuture.delayedExecutor(DISBAND_DELAY, TimeUnit.SECONDS, this.executor).execute(task);
        this.messagingManager.get().queuePacketAndFlush(() -> this.packetFactory.disbandParty(id));
    }

    @Override
    public void partyChangeMessageReceived(final PartyChangePacket pkt) {
        final @Nullable CompletableFuture<@Nullable Party> future = this.partyIfMemberOnline(pkt.partyId());
        if (future == null) {
            return;
        }
        future.thenAccept(party -> {
            if (party == null) {
                return;
            }
            final PartyImpl impl = (PartyImpl) party;
            pkt.changes().forEach((id, type) -> {
                switch (type) {
                    case ADD -> impl.addMemberRaw(id);
                    case REMOVE -> impl.removeMemberRaw(id);
                }
            });
        }).whenComplete(($, thr) -> {
            if (thr != null) {
                this.logger.warn("Exception handling party change packet {}", pkt, thr);
            }
        });
    }

    private @Nullable CompletableFuture<@Nullable Party> partyIfMemberOnline(final UUID partyId) {
        @Nullable CompletableFuture<@Nullable Party> future = this.partyCache.getIfPresent(partyId);
        if (future == null) {
            // we want to notify any online members even if the party isn't loaded locally yet
            for (final CarbonPlayer player : this.server.players()) {
                if (partyId.equals(((WrappedCarbonPlayer) player).partyId())) {
                    future = this.party(partyId);
                }
            }
        }
        return future;
    }

    @Override
    public void disbandPartyMessageReceived(final DisbandPartyPacket pkt) {
        final @Nullable CompletableFuture<@Nullable Party> future = this.partyIfMemberOnline(pkt.partyId());
        this.recentDisbands.put(pkt.partyId(), new Object());
        if (future == null) {
            return;
        }
        future.thenAccept(party -> {
            if (party == null) {
                return;
            }
            ((PartyImpl) party).disbandRaw();
            this.partyCache.synchronous().invalidate(pkt.partyId());
        }).whenComplete(($, thr) -> {
            if (thr != null) {
                this.logger.warn("Exception handling party disband packet {}", pkt, thr);
            }
        });
    }

}

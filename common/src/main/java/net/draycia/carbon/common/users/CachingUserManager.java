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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.draycia.carbon.common.util.ConcurrentUtil;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class CachingUserManager implements UserManagerInternal<CarbonPlayerCommon> {

    protected final Logger logger;
    protected final ExecutorService executor;
    private final ReentrantLock cacheLock;
    private final Map<UUID, CompletableFuture<CarbonPlayerCommon>> cache;

    protected CachingUserManager(final Logger logger, final ExecutorService executorService) {
        this.logger = logger;
        this.executor = executorService;
        this.cacheLock = new ReentrantLock();
        this.cache = new HashMap<>();
    }

    protected abstract CarbonPlayerCommon loadOrCreate(UUID uuid);

    @Override
    public CompletableFuture<CarbonPlayerCommon> user(final UUID uuid) {
        this.cacheLock.lock();
        try {
            return this.cache.computeIfAbsent(uuid, $ -> {
                final CompletableFuture<CarbonPlayerCommon> future = CompletableFuture.supplyAsync(() -> this.loadOrCreate(uuid), this.executor);
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
                    this.logger.warn("Exception saving data for player with uuid " + entry.getKey());
                }
            }
            ConcurrentUtil.shutdownExecutor(this.executor, TimeUnit.MILLISECONDS, 500);
        } finally {
            this.cacheLock.unlock();
        }
    }

    @Override
    public CompletableFuture<Void> loggedOut(final UUID uuid) {
        this.cacheLock.lock();
        try {
            final @Nullable CompletableFuture<CarbonPlayerCommon> remove = this.cache.remove(uuid);
            if (remove != null && remove.isDone()) { // don't need to save if it never finished loading
                final @Nullable CarbonPlayerCommon join = remove.join();
                if (join != null) {
                    return this.save(join);
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
                this.save(getNow);
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

}

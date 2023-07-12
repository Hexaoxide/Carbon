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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.serialisation.gson.UUIDSerializerGson;
import net.draycia.carbon.common.util.FileUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class ProfileCache {

    private static final long REMOVE_AFTER = Duration.ofDays(7).toMillis();
    private static final long REMOVE_NULL_IDS_AFTER = Duration.ofHours(1).toMillis();

    private final Gson gson;
    private final Path cacheFile;
    private final Map<UUID, CacheEntry> byId;
    private final Map<String, CacheEntry> byName;
    private final Set<CacheEntry> entries;

    private record CacheEntry(@Nullable UUID uuid, @Nullable String name, long updated) {

    }

    @Inject
    private ProfileCache(final @DataDirectory Path dataDirectory) {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDSerializerGson())
            .create();
        this.cacheFile = dataDirectory.resolve("users/profile_cache.json");
        this.byId = new HashMap<>();
        this.byName = new HashMap<>();
        this.entries = new HashSet<>();
        this.load();
    }

    public synchronized @Nullable String cachedName(final UUID id) {
        final @Nullable CacheEntry entry = this.byId.get(id);
        if (entry == null) {
            return null;
        } else if (entry.updated() < cutoff()) {
            return null;
        }
        return entry.name();
    }

    public synchronized @Nullable UUID cachedId(final String name) {
        final @Nullable CacheEntry entry = this.byName.get(name);
        if (entry == null) {
            return null;
        } else if (entry.updated() < cutoff()) {
            return null;
        }
        return entry.uuid();
    }

    public synchronized boolean hasCachedEntry(final String name) {
        final @Nullable CacheEntry entry = this.byName.get(name);
        if (entry == null) {
            return false;
        }
        return entry.updated() >= cutoff();
    }

    public synchronized boolean hasCachedEntry(final UUID uuid) {
        final @Nullable CacheEntry entry = this.byId.get(uuid);
        if (entry == null) {
            return false;
        }
        return entry.updated() >= cutoff();
    }

    public synchronized void cache(final @Nullable UUID uuid, final @Nullable String name) {
        final @Nullable CacheEntry r1 = uuid == null ? null : this.byId.remove(uuid);
        final @Nullable CacheEntry r2 = name == null ? null : this.byName.remove(name);
        if (r1 != null) {
            this.entries.remove(r1);
        }
        if (r2 != null) {
            this.entries.remove(r2);
        }
        final CacheEntry entry = new CacheEntry(uuid, name, System.currentTimeMillis());
        this.entries.add(entry);
        if (entry.name() != null) {
            this.byName.put(entry.name(), entry);
        }
        if (entry.uuid() != null) {
            this.byId.put(entry.uuid(), entry);
        }
    }

    private synchronized void cleanup() {
        final long cutoff = cutoff();
        final long nullIdCutoff = nullIdCutoff();
        for (final Iterator<CacheEntry> iterator = this.entries.iterator(); iterator.hasNext();) {
            final CacheEntry entry = iterator.next();
            if (entry.updated() < cutoff || entry.uuid() == null && entry.updated() < nullIdCutoff) {
                iterator.remove();
                if (entry.uuid() != null) {
                    this.byId.remove(entry.uuid());
                }
                if (entry.name() != null) {
                    this.byName.remove(entry.name());
                }
            }
        }
    }

    private static long nullIdCutoff() {
        return System.currentTimeMillis() - REMOVE_NULL_IDS_AFTER;
    }

    private static long cutoff() {
        return System.currentTimeMillis() - REMOVE_AFTER;
    }

    private synchronized void load() {
        this.entries.clear();
        this.byId.clear();
        this.byName.clear();
        if (!Files.exists(this.cacheFile)) {
            return;
        }
        try {
            try (final BufferedReader reader = Files.newBufferedReader(this.cacheFile)) {
                final Set<CacheEntry> load = this.gson.fromJson(reader, new TypeToken<Set<CacheEntry>>() {}.getType());
                this.entries.addAll(load);
                for (final CacheEntry entry : this.entries) {
                    if (entry.name() != null) {
                        this.byName.put(entry.name(), entry);
                    }
                    if (entry.uuid() != null) {
                        this.byId.put(entry.uuid(), entry);
                    }
                }
            }
        } catch (final IOException ex) {
            throw new RuntimeException("Failed to load cache", ex);
        }
    }

    public synchronized void save() {
        this.cleanup();
        try (final BufferedWriter writer = Files.newBufferedWriter(FileUtil.mkParentDirs(this.cacheFile))) {
            this.gson.toJson(this.entries, writer);
        } catch (final IOException ex) {
            throw new RuntimeException("Failed to save cache", ex);
        }
    }

}

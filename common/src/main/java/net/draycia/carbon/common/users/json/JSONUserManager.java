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
package net.draycia.carbon.common.users.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.serialisation.gson.ChatChannelSerializerGson;
import net.draycia.carbon.common.serialisation.gson.UUIDSerializerGson;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class JSONUserManager implements UserManagerInternal<CarbonPlayerCommon> {

    private static final String EMPTY_USER_UUID = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";
    private final Logger logger;
    private final Gson serializer;
    private final Path userDirectory;
    private final ProfileResolver profileResolver;
    private final Map<UUID, CompletableFuture<CarbonPlayerCommon>> cache = new HashMap<>();
    private final ExecutorService executor;
    private final String emptyUserJson;

    @Inject
    public JSONUserManager(
        final @ForCarbon Path dataDirectory,
        final Injector injector,
        final Logger logger,
        final ProfileResolver profileResolver
    ) throws IOException {
        this.logger = logger;
        this.userDirectory = dataDirectory.resolve("users");
        this.profileResolver = profileResolver;

        Files.createDirectories(this.userDirectory);

        this.serializer = GsonComponentSerializer.gson().populator()
            .apply(new GsonBuilder())
            .registerTypeAdapter(ChatChannel.class, injector.getInstance(ChatChannelSerializerGson.class))
            .registerTypeAdapter(UUID.class, injector.getInstance(UUIDSerializerGson.class))
            .setPrettyPrinting()
            .create();

        this.emptyUserJson = this.serializer.toJson(new CarbonPlayerCommon("username", UUID.fromString(EMPTY_USER_UUID)));

        this.executor = Executors.newSingleThreadExecutor(ConcurrentUtil.carbonThreadFactory(logger, "JSONUserManager"));
    }

    @Override
    public synchronized CompletableFuture<CarbonPlayerCommon> user(final UUID uuid) {
        return this.cache.computeIfAbsent(uuid, $ -> {
            final CompletableFuture<CarbonPlayerCommon> future = CompletableFuture.supplyAsync(() -> {
                final Path userFile = this.userFile(uuid);

                if (Files.exists(userFile)) {
                    try {
                        final @Nullable CarbonPlayerCommon player =
                            this.serializer.fromJson(Files.newBufferedReader(userFile), CarbonPlayerCommon.class);

                        if (player == null) {
                            throw new IllegalStateException("Player file found but was empty.");
                        }
                        player.profileResolver = this.profileResolver;
                        player.leftChannels().removeIf(channel -> CarbonChatProvider.carbonChat()
                            .channelRegistry()
                            .get(channel) == null);

                        return player;
                    } catch (final IOException exception) {
                        throw new RuntimeException(exception);
                    }
                }

                final CarbonPlayerCommon player = new CarbonPlayerCommon(null /* Username will be resolved when requested */, uuid);
                player.profileResolver = this.profileResolver;
                return player;
            }, this.executor);

            // Don't keep failed requests so they can be retried on the next request
            // The caller is expected to handle the error
            future.whenComplete((result, $$$) -> {
                if (result == null) {
                    synchronized (this) {
                        this.cache.remove(uuid);
                    }
                }
            });

            return future;
        });
    }

    private Path userFile(final UUID id) {
        return this.userDirectory.resolve(id + ".json");
    }

    @Override
    public CompletableFuture<Void> save(final CarbonPlayerCommon player) {
        return CompletableFuture.runAsync(() -> {
            final Path userFile = this.userFile(player.uuid());

            try {
                final String json = this.serializer.toJson(player);

                if (json == null || json.isBlank()) {
                    throw new IllegalStateException("No data to save - toJson returned null or blank.");

                    // quick and dirty check to not create files for/save default data
                } else if (json.equals(this.emptyUserJson.replace(EMPTY_USER_UUID, player.uuid().toString()))) {
                    return;
                }

                if (!Files.exists(userFile)) {
                    Files.createFile(userFile);
                }

                Files.writeString(userFile, json,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            } catch (final IOException exception) {
                throw new RuntimeException("Exception while saving data for player [%s]".formatted(player.username()), exception);
            }
        }, this.executor);
    }

    @Override
    public void shutdown() {
        for (final UUID id : List.copyOf(this.cache.keySet())) {
            try {
                this.loggedOut(id).join();
            } catch (final Exception ex) {
                this.logger.warn("Exception saving data for player with uuid " + id);
            }
        }
        ConcurrentUtil.shutdownExecutor(this.executor, TimeUnit.MILLISECONDS, 500);
    }

    @Override
    public synchronized CompletableFuture<Void> loggedOut(final UUID uuid) {
        final CompletableFuture<@Nullable CarbonPlayerCommon> remove = this.cache.remove(uuid);
        final @Nullable CarbonPlayerCommon join = remove.join();
        if (remove.isDone() && join != null) { // don't need to save if it never finished loading
            return this.save(join);
        }
        return CompletableFuture.completedFuture(null);
    }

}

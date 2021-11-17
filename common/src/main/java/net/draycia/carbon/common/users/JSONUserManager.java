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
package net.draycia.carbon.common.users;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.serialisation.gson.ChatChannelSerializerGson;
import net.draycia.carbon.common.serialisation.gson.UUIDSerializerGson;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public class JSONUserManager implements UserManager<CarbonPlayerCommon> {

    private final Logger logger;
    private final Gson serializer;
    private final Path userDirectory;
    private final CarbonChat carbonChat;

    private final Map<UUID, CarbonPlayerCommon> userCache = new ConcurrentHashMap<>();

    @Inject
    public JSONUserManager(
        final @ForCarbon Path dataDirectory,
        final Injector injector,
        final Logger logger,
        final CarbonChat carbonChat
    ) throws IOException {
        this.logger = logger;
        this.userDirectory = dataDirectory.resolve("users");
        this.carbonChat = carbonChat;

        Files.createDirectories(this.userDirectory);

        this.serializer = GsonComponentSerializer.gson().populator()
            .apply(new GsonBuilder())
            .registerTypeAdapter(ChatChannel.class, injector.getInstance(ChatChannelSerializerGson.class))
            .registerTypeAdapter(UUID.class, injector.getInstance(UUIDSerializerGson.class))
            .setPrettyPrinting()
            .create();
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> carbonPlayer(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable CarbonPlayerCommon cachedPlayer = this.userCache.get(uuid);

            if (cachedPlayer != null) {
                return new ComponentPlayerResult<>(cachedPlayer, empty());
            }

            final Path userFile = this.userDirectory.resolve(uuid + ".json");

            if (Files.exists(userFile)) {
                try {
                    final @Nullable CarbonPlayerCommon player =
                        this.serializer.fromJson(Files.newBufferedReader(userFile), CarbonPlayerCommon.class);

                    if (player == null) {
                        return new ComponentPlayerResult<>(null, text("Player file found but was empty."));
                    }

                    this.userCache.put(uuid, player);

                    // TODO: supply reason if fromJson returns null
                    return new ComponentPlayerResult<>(player, empty());
                } catch (final IOException exception) {
                    return new ComponentPlayerResult<>(null, text(exception.getMessage()));
                }
            }

            final String name = Objects.requireNonNull(
                this.carbonChat.server().resolveName(uuid).join());

            final CarbonPlayerCommon player = new CarbonPlayerCommon(name, uuid);

            this.userCache.put(uuid, player);

            return new ComponentPlayerResult<>(player, empty());
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> savePlayer(final CarbonPlayerCommon player) {
        return CompletableFuture.supplyAsync(() -> {
            final Path userFile = this.userDirectory.resolve(player.uuid() + ".json");

            try {
                final String json = this.serializer.toJson(player);

                if (json == null || json.isBlank()) {
                    return new ComponentPlayerResult<>(null, text("No data to save - toJson returned null or blank."));
                }

                if (!Files.exists(userFile)) {
                    Files.createFile(userFile);
                }

                Files.writeString(userFile, json,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);

                return new ComponentPlayerResult<>(player, text(String.format("Saving player data for [%s], [%s]",
                    player.username(), player.uuid())));
            } catch (final IOException exception) {
                this.logger.error("Exception caught while saving data for player [{}]", player.username());
                exception.printStackTrace();
                return new ComponentPlayerResult<>(null, text(exception.getMessage()));
            }
        });
    }

}

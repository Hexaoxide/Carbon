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
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.serialisation.gson.ChatChannelSerializerGson;
import net.draycia.carbon.common.serialisation.gson.UUIDSerializerGson;
import net.draycia.carbon.common.users.CachingUserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.PersistentUserProperty;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class JSONUserManager extends CachingUserManager {

    private final Gson serializer;
    private final Path userDirectory;

    @Inject
    public JSONUserManager(
        final @DataDirectory Path dataDirectory,
        final Logger logger,
        final ProfileResolver profileResolver,
        final MembersInjector<CarbonPlayerCommon> playerInjector,
        final ChatChannelSerializerGson channelSerializer,
        final UUIDSerializerGson uuidSerializer,
        final Provider<MessagingManager> messagingManager,
        final PacketFactory packetFactory
    ) throws IOException {
        super(
            logger,
            Executors.newSingleThreadExecutor(ConcurrentUtil.carbonThreadFactory(logger, "JSONUserManager")),
            profileResolver,
            playerInjector,
            messagingManager,
            packetFactory
        );
        this.userDirectory = dataDirectory.resolve("users");

        Files.createDirectories(this.userDirectory);

        this.serializer = GsonComponentSerializer.gson().populator()
            .apply(new GsonBuilder())
            .registerTypeAdapter(ChatChannel.class, channelSerializer)
            .registerTypeAdapter(UUID.class, uuidSerializer)
            .registerTypeAdapter(PersistentUserProperty.class, new PersistentUserProperty.Serializer())
            .setPrettyPrinting()
            .create();
    }

    @Override
    protected CarbonPlayerCommon loadOrCreate(final UUID uuid) {
        final Path userFile = this.userFile(uuid);

        if (Files.exists(userFile)) {
            try {
                final @Nullable CarbonPlayerCommon player;
                try (final Reader reader = Files.newBufferedReader(userFile)) {
                    player = this.serializer.fromJson(reader, CarbonPlayerCommon.class);
                }

                if (player == null) {
                    throw new IllegalStateException("Player file found but was empty.");
                }
                player.leftChannels().forEach(channel -> {
                    if (CarbonChatProvider.carbonChat()
                        .channelRegistry()
                        .get(channel) == null) {
                        player.joinChannel(channel, true);
                    }
                });

                return player;
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return new CarbonPlayerCommon(null /* Username will be resolved when requested */, uuid);
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
                }

                Files.writeString(userFile, json);
            } catch (final IOException exception) {
                throw new RuntimeException("Exception while saving data for player [%s]".formatted(player.username()), exception);
            }
        }, this.executor);
    }

}

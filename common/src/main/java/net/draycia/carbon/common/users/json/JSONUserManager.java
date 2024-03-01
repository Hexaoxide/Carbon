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
package net.draycia.carbon.common.users.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.serialisation.gson.ChatChannelSerializerGson;
import net.draycia.carbon.common.serialisation.gson.UUIDSerializerGson;
import net.draycia.carbon.common.users.CachingUserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.PartyImpl;
import net.draycia.carbon.common.users.PersistentUserProperty;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.util.Exceptions;
import net.draycia.carbon.common.util.FileUtil;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class JSONUserManager extends CachingUserManager {

    private final Gson serializer;
    private final Path userDirectory;
    private final Path partyDirectory;
    private final ChannelRegistry channelRegistry;

    @Inject
    public JSONUserManager(
        final @DataDirectory Path dataDirectory,
        final Logger logger,
        final ProfileResolver profileResolver,
        final Injector injector,
        final ChatChannelSerializerGson channelSerializer,
        final UUIDSerializerGson uuidSerializer,
        final Provider<MessagingManager> messagingManager,
        final PacketFactory packetFactory,
        final CarbonChannelRegistry channelRegistry,
        final CarbonServer server
    ) throws IOException {
        super(
            logger,
            profileResolver,
            injector,
            messagingManager,
            packetFactory,
            server
        );
        this.userDirectory = dataDirectory.resolve("users");
        this.partyDirectory = dataDirectory.resolve("party");
        this.channelRegistry = channelRegistry;

        Files.createDirectories(this.userDirectory);
        Files.createDirectories(this.partyDirectory);

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
                    if (this.channelRegistry.channel(channel) == null) {
                        player.joinChannel(channel, true);
                    }
                });

                return player;
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return new CarbonPlayerCommon(null, uuid);
    }

    private Path userFile(final UUID id) {
        return this.userDirectory.resolve(id + ".json");
    }

    private Path partyFile(final UUID id) {
        return this.partyDirectory.resolve(id + ".json");
    }

    @Override
    public void saveSync(final CarbonPlayerCommon player) {
        final Path userFile = this.userFile(player.uuid());

        try {
            final String json = this.serializer.toJson(player);

            if (json == null || json.isBlank()) {
                throw new IllegalStateException("No data to save - toJson returned null or blank.");
            }

            Files.writeString(FileUtil.mkParentDirs(userFile), json);
        } catch (final IOException exception) {
            throw new RuntimeException("Exception while saving data for player [%s]".formatted(player.username()), exception);
        }
    }

    @Override
    protected @Nullable PartyImpl loadParty(final UUID uuid) {
        final Path partyFile = this.partyFile(uuid);

        if (Files.exists(partyFile)) {
            try {
                final @Nullable PartyImpl party;
                try (final Reader reader = Files.newBufferedReader(partyFile)) {
                    party = this.serializer.<@Nullable PartyImpl>fromJson(reader, PartyImpl.class);
                }

                if (party == null) {
                    throw new IllegalStateException("Party file found but was empty.");
                }

                return party;
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return null;
    }

    @Override
    protected void saveSync(final PartyImpl party, final Map<UUID, PartyImpl.ChangeType> changes) {
        final Path partyFile = this.partyFile(party.id());

        try {
            final String json = this.serializer.toJson(party);

            if (json == null || json.isBlank()) {
                throw new IllegalStateException("No data to save - toJson returned null or blank.");
            }

            Files.writeString(FileUtil.mkParentDirs(partyFile), json);
        } catch (final IOException exception) {
            throw new RuntimeException("Exception while saving data for party " + party, exception);
        }
    }

    @Override
    public void disbandSync(final UUID id) {
        try {
            Files.deleteIfExists(this.partyFile(id));
        } catch (final IOException ex) {
            Exceptions.rethrow(ex);
        }
    }

}

package net.draycia.carbon.common.users;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.serialisation.gson.CarbonPlayerSerializerGson;
import net.draycia.carbon.common.serialisation.gson.ChatChannelSerializerGson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public class JSONUserManager implements UserManager {

    private final Path dataDirectory;
    private final Injector injector;
    private final Logger logger;
    private final Gson serializer;
    private final Path userDirectory;

    @Inject
    public JSONUserManager(
        final @ForCarbon Path dataDirectory,
        final Injector injector,
        final Logger logger
    ) throws IOException {
        this.dataDirectory = dataDirectory;
        this.injector = injector;
        this.logger = logger;
        this.userDirectory = this.dataDirectory.resolve("users");

        Files.createDirectories(this.userDirectory);

        this.serializer = GsonComponentSerializer.gson().populator()
            .apply(new GsonBuilder())
            .registerTypeAdapter(ChatChannel.class, this.injector.getInstance(ChatChannelSerializerGson.class))
            .registerTypeAdapter(CarbonPlayer.class, this.injector.getInstance(CarbonPlayerSerializerGson.class))
            .create();
    }

    @Override
    public CompletableFuture<PlayerResult> carbonPlayer(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final Path userFile = this.userDirectory.resolve(uuid + ".json");

            try {
                final CarbonPlayer player =
                    this.serializer.fromJson(Files.newBufferedReader(userFile), CarbonPlayer.class);

                // TODO: supply reason if fromJson returns null
                return new JSONPlayerResult(player, empty());
            } catch (final IOException exception) {
                return new JSONPlayerResult(null, text(exception.getMessage()));
            }
        });
    }

    @Override
    public CompletableFuture<PlayerResult> savePlayer(final CarbonPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            this.logger.info("Saving player data for [{}], [{}]", player.username(), player.uuid());
            final Path userFile = this.userDirectory.resolve(player.uuid() + ".json");

            try {
                final String json = this.serializer.toJson(player, CarbonPlayer.class);
                Files.writeString(userFile, json, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
                //this.serializer.toJson(player, CarbonPlayer.class, Files.newBufferedWriter(userFile));

                return new JSONPlayerResult(player, text(String.format("Saving player data for [%s], [%s]",
                    player.username(), player.uuid())));
            } catch (final IOException exception) {
                return new JSONPlayerResult(null, text(exception.getMessage()));
            }
        });
    }

    private final static class JSONPlayerResult implements PlayerResult {

        private final Component reason;
        private final @Nullable CarbonPlayer player;

        private JSONPlayerResult(final @Nullable CarbonPlayer player, final Component reason) {
            this.reason = reason;
            this.player = player;
        }

        @Override
        public boolean successful() {
            return this.player != null;
        }

        @Override
        public Component reason() {
            return this.reason;
        }

        @Override
        public @Nullable CarbonPlayer player() {
            return this.player;
        }
    }

}

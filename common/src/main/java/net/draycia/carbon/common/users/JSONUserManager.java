package net.draycia.carbon.common.users;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.serialisation.gson.ChatChannelSerializerGson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public class JSONUserManager implements UserManager {

    private final Path dataDirectory;
    private final Injector injector;
    private final Gson serializer;
    private final Path userDirectory;

    @Inject
    public JSONUserManager(
        final @ForCarbon Path dataDirectory,
        final Injector injector
    ) throws IOException {
        this.dataDirectory = dataDirectory;
        this.injector = injector;
        this.userDirectory = this.dataDirectory.resolve("users");

        Files.createDirectories(this.userDirectory);

        this.serializer = GsonComponentSerializer.gson().populator()
            .apply(new GsonBuilder())
            .registerTypeAdapter(CarbonChat.class, this.injector.getInstance(ChatChannelSerializerGson.class))
            .create();
    }

    @Override
    public CompletableFuture<PlayerResult> carbonPlayer(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final Path userFile = this.userDirectory.resolve(uuid + ".json");

            try {
                final CarbonPlayer player =
                    this.serializer.fromJson(Files.newBufferedReader(userFile), CarbonPlayer.class);

                return new JSONPlayerResult(player, empty());
            } catch (final IOException exception) {
                return new JSONPlayerResult(null, text(exception.getMessage()));
            }
        });
    }

    @Override
    public CompletableFuture<PlayerResult> savePlayer(final CarbonPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            final Path userFile = this.userDirectory.resolve(player.uuid() + ".json");

            try {
                this.serializer.toJson(player, CarbonPlayer.class, Files.newBufferedWriter(userFile));

                return new JSONPlayerResult(player, empty());
            } catch (final IOException exception) {
                return new JSONPlayerResult(null, text(exception.getMessage()));
            }
        });
    }

    private final static class JSONPlayerResult implements PlayerResult {

        private final boolean successful;
        private final Component reason;
        private final @Nullable CarbonPlayer player;

        private JSONPlayerResult(final @Nullable CarbonPlayer player, final Component reason) {
            this.successful = player == null;
            this.reason = reason;
            this.player = player;
        }

        @Override
        public boolean successful() {
            return this.successful;
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

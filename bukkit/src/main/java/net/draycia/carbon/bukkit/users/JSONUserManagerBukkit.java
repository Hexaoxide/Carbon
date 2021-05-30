package net.draycia.carbon.bukkit.users;

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
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class JSONUserManagerBukkit implements UserManager {

    private final Path dataDirectory;
    private final Injector injector;
    private final Gson serializer;
    private final Path userDirectory;

    @Inject
    public JSONUserManagerBukkit(
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
    public CompletableFuture<@Nullable CarbonPlayer> carbonPlayer(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final Path userFile = this.userDirectory.resolve(uuid + ".json");

            try {
                return this.serializer.fromJson(Files.newBufferedReader(userFile), CarbonPlayer.class);
            } catch (final IOException ignored) {
                return null;
            }
        });
    }

}

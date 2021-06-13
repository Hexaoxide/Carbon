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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.serialisation.gson.CarbonPlayerSerializerGson;
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
public class JSONUserManager implements UserManager {

    private final Path dataDirectory;
    private final Injector injector;
    private final Logger logger;
    private final Gson serializer;
    private final Path userDirectory;

    private final Map<UUID, CarbonPlayerCommon> userCache = new ConcurrentHashMap<>();

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
            .registerTypeAdapter(CarbonPlayerCommon.class, this.injector.getInstance(CarbonPlayerSerializerGson.class))
            .registerTypeAdapter(ChatChannel.class, this.injector.getInstance(ChatChannelSerializerGson.class))
            .registerTypeAdapter(UUID.class, this.injector.getInstance(UUIDSerializerGson.class))
            .create();
    }

    @Override
    public CompletableFuture<ComponentPlayerResult> carbonPlayer(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable CarbonPlayerCommon cachedPlayer = this.userCache.get(uuid);

            if (cachedPlayer != null) {
                return  new ComponentPlayerResult(cachedPlayer, empty());
            }

            final Path userFile = this.userDirectory.resolve(uuid + ".json");

            if (Files.exists(userFile)) {
                try {
                    final CarbonPlayerCommon player =
                        this.serializer.fromJson(Files.newBufferedReader(userFile), CarbonPlayerCommon.class);

                    this.userCache.put(uuid, player);

                    // TODO: supply reason if fromJson returns null
                    return new ComponentPlayerResult(player, empty());
                } catch (final IOException exception) {
                    return new ComponentPlayerResult(null, text(exception.getMessage()));
                }
            }

            return new ComponentPlayerResult(null, text("No save file found for player."));
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult> savePlayer(final CarbonPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            this.logger.info("Saving player data for [{}], [{}]", player.username(), player.uuid());
            final Path userFile = this.userDirectory.resolve(player.uuid() + ".json");

            try {
                if (!Files.exists(userFile)) {
                    Files.createFile(userFile);
                }

                try {
                    final String json = this.serializer.toJson(player, CarbonPlayerCommon.class);

                    Files.writeString(userFile, json,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                } catch (final ClassCastException exception) {
                    exception.printStackTrace();
                }

                return new ComponentPlayerResult(player, text(String.format("Saving player data for [%s], [%s]",
                    player.username(), player.uuid())));
            } catch (final IOException exception) {
                exception.printStackTrace();
                return new ComponentPlayerResult(null, text(exception.getMessage()));
            }
        });
    }

}

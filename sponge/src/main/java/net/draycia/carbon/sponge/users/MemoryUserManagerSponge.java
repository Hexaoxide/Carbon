package net.draycia.carbon.sponge.users;

import com.google.inject.Singleton;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

/**
 * This class should not be used.
 * No data is persisted or saved.
 * This exists merely as a placeholder.
 */
@Singleton
public final class MemoryUserManagerSponge implements UserManager {

    private final @NonNull Map<UUID, @NonNull CarbonPlayer> users = new HashMap<>();

    @Override
    public @Nullable CarbonPlayer carbonPlayer(final @NonNull UUID uuid) {
        if (this.users.containsKey(uuid)) {
            return this.users.get(uuid);
        }

        final Optional<ServerPlayer> player = Sponge.server().player(uuid);

        if (player.isEmpty()) {
            return null;
        }

        final CarbonPlayer carbonPlayer = new CarbonPlayerSponge(player.get().name(),
            text(player.get().name()), player.get().uniqueId());

        this.users.put(uuid, carbonPlayer);

        return carbonPlayer;
    }

    @Override
    public @Nullable CarbonPlayer carbonPlayer(final @NonNull String username) {
        final Optional<ServerPlayer> player = Sponge.server().player(username);

        if (player.isEmpty()) {
            return null;
        }

        if (this.users.containsKey(player.get().uniqueId())) {
            return this.users.get(player.get().uniqueId());
        }

        return new CarbonPlayerSponge(player.get().name(), text(player.get().name()),
            player.get().uniqueId());
    }

}

package net.draycia.carbon.velocity.users;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class should not be used.
 * No data is persisted or saved.
 * This exists merely as a placeholder.
 */
@Singleton
public final class MemoryUserManagerVelocity implements UserManager {

    private final ProxyServer server;

    private final @NonNull Map<UUID, @NonNull CarbonPlayer> users = new HashMap<>();

    @Inject
    public MemoryUserManagerVelocity(final ProxyServer server) {
        this.server = server;
    }

    @Override
    public @Nullable CarbonPlayer carbonPlayer(final @NonNull UUID uuid) {
        if (this.users.containsKey(uuid)) {
            return this.users.get(uuid);
        }

        final Optional<Player> player = server.getPlayer(uuid);

        if (player.isEmpty()) {
            return null;
        }

        return this.users.computeIfAbsent(player.get().getUniqueId(), key ->
            new CarbonPlayerVelocity(player.get().getUsername(), player.get().getUniqueId(), server));
    }

    @Override
    public @Nullable CarbonPlayer carbonPlayer(final @NonNull String username) {
        final Optional<Player> player = server.getPlayer(username);

        if (player.isEmpty()) {
            return null;
        }

        return this.carbonPlayer(player.get().getUniqueId());
    }

}

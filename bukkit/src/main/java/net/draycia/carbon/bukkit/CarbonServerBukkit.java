package net.draycia.carbon.bukkit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.users.CarbonPlayerBukkit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerBukkit implements CarbonServer, ForwardingAudience.Single {

    private final Map<UUID, CarbonPlayerBukkit> userCache = new HashMap<>();
    private final CarbonChatBukkitEntry chatBukkitEntry;
    private final UserManager userManager;

    @Inject
    private CarbonServerBukkit(final CarbonChatBukkitEntry chatBukkitEntry, final UserManager userManager) {
        this.chatBukkitEntry = chatBukkitEntry;
        this.userManager = userManager;
    }

    @Override
    public Audience audience() {
        return Audience.audience(this.console(), Audience.audience(this.players()));
    }

    @Override
    public Audience console() {
        return this.chatBukkitEntry.getServer().getConsoleSender();
    }

    @Override
    public Iterable<? extends CarbonPlayer> players() {
        final var players = new ArrayList<CarbonPlayer>();

        for (final var player : this.chatBukkitEntry.getServer().getOnlinePlayers()) {
            final @Nullable CarbonPlayer carbonPlayer = this.player(player).join();

            if (carbonPlayer != null) {
                players.add(carbonPlayer);
            }
        }

        return players;
    }

    @Override
    public CompletableFuture<@Nullable CarbonPlayer> player(final UUID uuid) {
        final CarbonPlayerBukkit carbonPlayerBukkit = this.userCache.get(uuid);

        if (carbonPlayerBukkit != null) {
            return CompletableFuture.completedFuture(carbonPlayerBukkit);
        }

        return CompletableFuture.supplyAsync(() -> {
            final @Nullable CarbonPlayer carbonPlayer = this.userManager.carbonPlayer(uuid).join();

            if (carbonPlayer != null) {
                return new CarbonPlayerBukkit(carbonPlayer);
            }

            @Nullable String name = Bukkit.getOfflinePlayer(uuid).getName();

            if (name != null) {
                return new CarbonPlayerBukkit(Identity.identity(uuid), name, uuid);
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<@Nullable CarbonPlayer> player(final String username) {
        for (final var bukkitPlayer : this.userCache.values()) {
            if (bukkitPlayer.username().equalsIgnoreCase(username)) {
                return CompletableFuture.completedFuture(bukkitPlayer);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            final @Nullable UUID uuid = this.resolveUUID(username).join();

            if (uuid != null) {
                return this.player(uuid).join();
            }

            return null;
        });
    }

    private CompletableFuture<@Nullable CarbonPlayer> player(final Player player) {
        return this.player(player.getUniqueId());
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        // TODO: user cache?
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(username).getUniqueId());
    }

}

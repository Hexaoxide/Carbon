package net.draycia.carbon.bukkit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.users.CarbonPlayerBukkit;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerBukkit implements CarbonServer, ForwardingAudience.Single {

    private final CarbonChatBukkit chatBukkitEntry;
    private final UserManager userManager;

    @Inject
    private CarbonServerBukkit(final CarbonChatBukkit chatBukkitEntry, final UserManager userManager) {
        this.chatBukkitEntry = chatBukkitEntry;
        this.userManager = userManager;
    }

    @Override
    public Audience audience() {
        return this.chatBukkitEntry.getServer();
    }

    @Override
    public Audience console() {
        return this.chatBukkitEntry.getServer().getConsoleSender();
    }

    @Override
    public Iterable<? extends CarbonPlayer> players() {
        final var players = new ArrayList<CarbonPlayer>();

        for (final var player : this.chatBukkitEntry.getServer().getOnlinePlayers()) {
            final @Nullable ComponentPlayerResult result = this.player(player).join();

            if (result.player() != null) {
                players.add(result.player());
            }
        }

        return players;
    }

    private CompletableFuture<ComponentPlayerResult> wrapPlayer(final UUID uuid) {
        return this.userManager.carbonPlayer(uuid).thenCompose(result -> {
            return CompletableFuture.supplyAsync(() -> {
                if (result.player() != null) {
                    new ComponentPlayerResult(new CarbonPlayerBukkit(result.player()), Component.empty());
                }

                final @Nullable String name = this.resolveName(uuid).join();

                if (name != null) {
                    final CarbonPlayerCommon player = new CarbonPlayerCommon(null,
                        null, name, uuid);

                    return new ComponentPlayerResult(new CarbonPlayerBukkit(player), Component.empty());
                }

                return new ComponentPlayerResult(null, text("Name not found for uuid!"));
            });
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult> player(final UUID uuid) {
        return this.wrapPlayer(uuid);
    }

    @Override
    public CompletableFuture<ComponentPlayerResult> player(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable UUID uuid = this.resolveUUID(username).join();

            if (uuid != null) {
                return this.player(uuid).join();
            }

            return new ComponentPlayerResult(null, text("No UUID found for name."));
        });
    }

    public CompletableFuture<ComponentPlayerResult> player(final Player player) {
        return this.player(player.getUniqueId());
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        // TODO: user cache?
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(username).getUniqueId());
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        // TODO: user cache?
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(uuid).getName());
    }

}

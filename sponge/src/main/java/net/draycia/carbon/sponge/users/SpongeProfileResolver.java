package net.draycia.carbon.sponge.users;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.users.MojangProfileResolver;
import net.draycia.carbon.common.users.ProfileResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

@Singleton
@DefaultQualifier(NonNull.class)
public class SpongeProfileResolver implements ProfileResolver {

    private final ProfileResolver mojang;

    @Inject
    private SpongeProfileResolver(final MojangProfileResolver mojang) {
        this.mojang = mojang;
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username, final boolean cacheOnly) {
        final Optional<ServerPlayer> player = Sponge.server().player(username);

        if (player.isPresent()) {
            return CompletableFuture.completedFuture(player.get().uniqueId());
        }

        return this.mojang.resolveUUID(username, cacheOnly);
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid, final boolean cacheOnly) {
        final Optional<ServerPlayer> player = Sponge.server().player(uuid);

        if (player.isPresent()) {
            return CompletableFuture.completedFuture(player.get().name());
        }

        return this.mojang.resolveName(uuid, cacheOnly);
    }

    @Override
    public void shutdown() {
        this.mojang.shutdown();
    }

}

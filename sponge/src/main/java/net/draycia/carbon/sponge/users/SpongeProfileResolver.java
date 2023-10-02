package net.draycia.carbon.sponge.users;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.users.MojangProfileResolver;
import net.draycia.carbon.common.users.ProfileResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;

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
        return Sponge.server().gameProfileManager().basicProfile(username).thenApply(GameProfile::uuid).exceptionallyCompose(throwable ->
            this.mojang.resolveUUID(username, cacheOnly));
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid, final boolean cacheOnly) {
        return Sponge.server().gameProfileManager().basicProfile(uuid).thenApply(profile -> profile.name().orElseThrow()).exceptionallyCompose(throwable ->
            this.mojang.resolveName(uuid, cacheOnly));
    }

    @Override
    public void shutdown() {
        this.mojang.shutdown();
    }

}

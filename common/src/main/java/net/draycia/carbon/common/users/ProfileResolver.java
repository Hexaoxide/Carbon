package net.draycia.carbon.common.users;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface ProfileResolver {

    CompletableFuture<@Nullable UUID> resolveUUID(String username);

    CompletableFuture<@Nullable String> resolveName(UUID uuid);

    void shutdown();

}

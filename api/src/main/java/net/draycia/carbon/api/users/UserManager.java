package net.draycia.carbon.api.users;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Manager used to load and obtain {@link CarbonPlayer CarbonPlayers}.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface UserManager {

    /**
     * Loads and returns a {@link CarbonPlayer} with the given {@link UUID}.
     *
     * @param uuid The player's UUID.
     * @return The {@link CarbonPlayer}, or null if the player doesn't exist.
     * @since 2.0.0
     */
    CompletableFuture<@Nullable CarbonPlayer> carbonPlayer(final UUID uuid);

}

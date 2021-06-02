package net.draycia.carbon.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * The server that carbon is running on.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface CarbonServer extends Audience {

    /**
     * The server's console.
     *
     * @return the server's console
     * @since 2.0.0
     */
    Audience console();

    /**
     * The players that are online on the server.
     *
     * @return the online players
     * @since 2.0.0
     */
    Iterable<? extends CarbonPlayer> players();

    /**
     * Obtains a {@link CarbonPlayer} instance for the specified uuid.
     *
     * @param uuid the player's uuid
     * @return the player
     * @since 2.0.0
     */
    CompletableFuture<@Nullable CarbonPlayer> player(final UUID uuid);

    /**
     * Obtains a {@link CarbonPlayer} instance for the specified username.
     *
     * @param username the player's username
     * @return the player
     * @since 2.0.0
     */
    CompletableFuture<@Nullable CarbonPlayer> player(final String username);

    /**
     * Obtains the desired user's UUID.
     *
     * @param username the user's username
     * @return the user's UUID
     * @since 2.0.0
     */
    CompletableFuture<@Nullable UUID> resolveUUID(final String username);

}

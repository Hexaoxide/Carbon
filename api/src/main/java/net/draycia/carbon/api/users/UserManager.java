package net.draycia.carbon.api.users;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Manager used to load/obtain and save {@link CarbonPlayer CarbonPlayers}.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface UserManager {

    /**
     * Loads and returns a {@link CarbonPlayer} with the given {@link UUID}.
     *
     * @param uuid the player's uuid
     * @return the result
     * @since 2.0.0
     */
    CompletableFuture<ComponentPlayerResult> carbonPlayer(final UUID uuid);

    /**
     * Saves the {@link CarbonPlayer} and returns the result.
     *
     * @param player the player to save
     * @return the result
     * @since 2.0.0
     */
    CompletableFuture<ComponentPlayerResult> savePlayer(final CarbonPlayer player);

}

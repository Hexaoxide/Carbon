package net.draycia.carbon.api.users;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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
    CompletableFuture<PlayerResult> carbonPlayer(final UUID uuid);

    /**
     * Saves the {@link CarbonPlayer} and returns the result.
     *
     * @param player the player to save
     * @return the result
     * @since 2.0.0
     */
    CompletableFuture<PlayerResult> savePlayer(final CarbonPlayer player);

    /**
     * The result of a player data operation.
     *
     * @since 2.0.0
     */
    interface PlayerResult {

        /**
         * If the operation was successful.
         * If the operation was unsuccessful, {@link PlayerResult#reason()} will say why.
         *
         * @return if the operation was successful
         * @since 2.0.0
         */
        boolean successful();

        /**
         * The reason of the result.
         * Typically empty unless successful is false.
         *
         * @return the result's reason
         * @since 2.0.0
         */
        Component reason();

        /**
         * The {@link CarbonPlayer}, or null if unsuccessful.
         *
         * @return the player
         * @since 2.0.0
         */
        @Nullable CarbonPlayer player();

    }

}

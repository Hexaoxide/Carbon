package net.draycia.carbon.api.users;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The result of a player data operation.
 *
 * @since 2.0.0
 */
public record ComponentPlayerResult(
    /**
     * The {@link CarbonPlayer}, or null if unsuccessful.
     *
     * @return the player
     * @since 2.0.0
     */
    @Nullable CarbonPlayer player,

    /**
     * The reason of the result.
     * Typically empty unless successful is false.
     *
     * @return the result's reason
     * @since 2.0.0
     */
    Component reason
) {

}

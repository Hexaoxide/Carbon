package net.draycia.carbon.api.users;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The result of a player data operation.
 *
 * @param player the {@link CarbonPlayer}, or null if unsuccessful
 * @param reason the reason of the result, typically empty unless {@link #player} is null
 * @since 2.0.0
 */
public record ComponentPlayerResult(@Nullable CarbonPlayer player, Component reason) {

}

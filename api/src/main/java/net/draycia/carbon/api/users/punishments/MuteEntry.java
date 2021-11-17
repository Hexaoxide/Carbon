package net.draycia.carbon.api.users.punishments;

import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * An entry describing when and why and how a player was muted.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public record MuteEntry(
    long muteEpoch,
    @Nullable UUID muteCause,
    long expirationEpoch,
    @Nullable String reason,
    @Nullable Key channel,
    UUID muteId
) {

    /**
     * Returns if this entry is still active and should be enforced.
     *
     * @return if this entry is still valid
     * @since 2.0.0
     */
    public boolean valid() {
        return this.expirationEpoch == -1 || System.currentTimeMillis() < this.expirationEpoch;
    }

}

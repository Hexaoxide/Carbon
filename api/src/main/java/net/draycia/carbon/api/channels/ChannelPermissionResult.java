/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.api.channels;

import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Represents the result of a channel permission check.
 *
 * @since 3.0.0
 */
@DefaultQualifier(NonNull.class)
public interface ChannelPermissionResult {

    /**
     * Check whether the action checked was permitted.
     *
     * @return permitted
     * @since 3.0.0
     */
    boolean permitted();

    /**
     * Reason for permission being denied. When the action
     * was permitted, this should be equal to {@link Component#empty()}.
     *
     * @return deny reason
     * @since 3.0.0
     */
    Component reason();

    /**
     * Returns a result denoting that the player is permitted for the action.
     *
     * @return that the action is allowed
     * @since 3.0.0
     */
    static ChannelPermissionResult allowed() {
        return ChannelPermissionResultImpl.ALLOWED;
    }

    /**
     * Returns a result denoting that the action is denied for the player.
     *
     * @param reason the reason the action was denied
     * @return that the action is denied
     * @since 3.0.0
     */
    static ChannelPermissionResult denied(final Component reason) {
        return new ChannelPermissionResultImpl(false, () -> reason);
    }

    /**
     * Returns a result denoting that the action is denied for the player.
     *
     * @param reason the reason the action was denied
     * @return that the action is denied
     * @since 3.0.0
     */
    static ChannelPermissionResult denied(final Supplier<Component> reason) {
        return new ChannelPermissionResultImpl(false, reason);
    }

    /**
     * Create a {@link ChannelPermissionResult} based on {@code allowed},
     * computing {@code denyReason} when needed.
     *
     * @param allowed    whether the result is allowed
     * @param denyReason deny reason supplier
     * @return permission result
     * @since 3.0.0
     */
    static ChannelPermissionResult channelPermissionResult(
        final boolean allowed,
        final Supplier<Component> denyReason
    ) {
        if (allowed) {
            return allowed();
        }
        return denied(denyReason);
    }

}

/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
package net.draycia.carbon.api.util;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer used to construct chat components on a per-player basis.
 *
 * @since 2.0.0
 */
@FunctionalInterface
@DefaultQualifier(NonNull.class)
public interface ChatComponentRenderer {

    /**
     * Renders a Component for the specified recipient.
     *
     * @param sender          the player that sent the message
     * @param recipient       a recipient of the message.
     *                        may be a player, console, or other Audience implementations
     * @param message         the message being sent
     * @param originalMessage the original message that was sent
     * @return the component to be shown to the recipient,
     *     or empty if the recipient should not receive the message
     * @since 2.0.0
     */
    @NotNull
    RenderedMessage render(final CarbonPlayer sender,
                     final Audience recipient,
                     final Component message,
                     final Component originalMessage);

}

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
package net.draycia.carbon.api.event.events;

import com.seiama.event.Cancellable;
import java.util.Objects;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Called whenever a player privately messages another player.
 *
 * @since 2.1.0
 */
@DefaultQualifier(NonNull.class)
public class CarbonPrivateChatEvent implements CarbonEvent, Cancellable {

    private final CarbonPlayer sender;
    private final CarbonPlayer recipient;

    private Component message;

    private boolean cancelled = false;

    /**
     * Called whenever a player privately messages another player.
     *
     * @param sender the message sender
     * @param recipient the message recipient
     * @param message the message
     * @since 2.1.0
     */
    public CarbonPrivateChatEvent(final CarbonPlayer sender, final CarbonPlayer recipient, final Component message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    /**
     * Sets the message that will be sent.
     *
     * @param message the new message
     * @throws NullPointerException if message is null
     * @since 2.1.0
     */
    public void message(final Component message) {
        this.message = Objects.requireNonNull(message);
    }

    /**
     * The message that will be sent.
     *
     * @return the message
     * @since 2.1.0
     */
    public Component message() {
        return this.message;
    }

    /**
     * The message sender.
     *
     * @return the sender of the message
     * @since 2.1.0
     */
    public CarbonPlayer sender() {
        return this.sender;
    }

    /**
     * The message recipient.
     *
     * @return the recipient of the message
     * @since 2.1.0
     */
    public CarbonPlayer recipient() {
        return this.recipient;
    }

    @Override
    public boolean cancelled() {
        return this.cancelled;
    }

    @Override
    public void cancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

}

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

import java.util.List;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.Cancellable;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Event that's called when chat components are rendered for online players.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface CarbonChatEvent extends CarbonEvent, Cancellable {

    /**
     * Get the renderers used to construct components for each of the recipients. The returned list
     * is mutable.
     *
     * @return renderers
     * @since 2.0.0
     */
    List<KeyedRenderer> renderers();

    /**
     * The message's signature
     *
     * @return the signed message, or null if the message isn't signed
     * @since 2.1.0
     */
    @MonotonicNonNull SignedMessage signedMessage();

    /**
     * Get the sender of the message.
     *
     * @return The message sender.
     * @since 2.0.0
     */
    CarbonPlayer sender();

    /**
     * Get the original message that was sent.
     *
     * @return The original message.
     * @since 2.0.0
     */
    Component originalMessage();

    /**
     * Get the chat message that will be sent.
     *
     * @return The chat message.
     * @since 2.0.0
     */
    Component message();

    /**
     * Set the chat message that will be sent.
     *
     * @param message new message
     * @since 2.0.0
     */
    void message(final Component message);

    /**
     * The chat channel the message was sent in.
     *
     * @return the chat channel
     * @since 2.0.0
     */
    @MonotonicNonNull ChatChannel chatChannel();

    /**
     * The recipients of the message.
     * List is mutable and elements may be added/removed.
     *
     * @return the recipients of the message.
     *     entries may be players, console, or other audience implementations
     * @since 2.0.0
     */
    List<? extends Audience> recipients();

}

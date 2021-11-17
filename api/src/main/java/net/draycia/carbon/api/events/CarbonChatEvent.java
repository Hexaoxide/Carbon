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
package net.draycia.carbon.api.events;

import java.util.List;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.empty;

/**
 * {@link ResultedCarbonEvent} that's called when chat components are rendered for online players.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public class CarbonChatEvent implements ResultedCarbonEvent<CarbonChatEvent.Result> {

    private final List<KeyedRenderer> renderers;
    private final CarbonPlayer sender;
    private final Component originalMessage;
    private final List<? extends Audience> recipients;
    private final ChatChannel chatChannel;
    private Component message;
    private Result result = Result.ALLOWED;

    /**
     * {@link ResultedCarbonEvent} that's called when players send messages in chat.
     *
     * @param sender          the sender of the message
     * @param originalMessage the original message that was sent
     * @param recipients      the recipients of the message
     * @param renderers       the renderers of the message
     * @param chatChannel     the channel the message was sent in
     * @since 2.0.0
     */
    public CarbonChatEvent(
        final CarbonPlayer sender,
        final Component originalMessage,
        final List<? extends Audience> recipients,
        final List<KeyedRenderer> renderers,
        final ChatChannel chatChannel
    ) {
        this.sender = sender;
        this.originalMessage = originalMessage;
        this.message = originalMessage;
        this.recipients = recipients;
        this.renderers = renderers;
        this.chatChannel = chatChannel;
    }

    /**
     * Get the renderers used to construct components for each of the recipients.
     *
     * @return The per-recipient component renderers.
     * @since 2.0.0
     */
    public List<KeyedRenderer> renderers() {
        return this.renderers;
    }

    /**
     * Get the sender of the message.
     *
     * @return The message sender.
     * @since 2.0.0
     */
    public CarbonPlayer sender() {
        return this.sender;
    }

    /**
     * Get the original message that was sent.
     *
     * @return The original message.
     * @since 2.0.0
     */
    public Component originalMessage() {
        return this.originalMessage;
    }

    /**
     * Get the chat message that will be sent.
     *
     * @return The chat message.
     * @since 2.0.0
     */
    public Component message() {
        return this.message;
    }

    /**
     * Set the chat message that will be sent.
     *
     * @param message new message
     * @since 2.0.0
     */
    public void message(final Component message) {
        this.message = message;
    }

    /**
     * The chat channel the message was sent in.
     *
     * @return the chat channel
     * @since 2.0.0
     */
    public ChatChannel chatChannel() {
        return this.chatChannel;
    }

    /**
     * The recipients of the message.
     * List is mutable and elements may be added/removed.
     *
     * @return the recipients of the message.
     *     entries may be players, console, or other audience implementations
     * @since 2.0.0
     */
    public List<? extends Audience> recipients() {
        return this.recipients;
    }

    @Override
    public Result result() {
        return this.result;
    }

    @Override
    public void result(final Result result) {
        this.result = result;
    }

    /**
     * The result of this event.
     *
     * @since 2.0.0
     */
    public record Result(boolean cancelled, Component reason) implements ResultedCarbonEvent.Result {

        private static final Result ALLOWED = new Result(false, empty());

        /**
         * Returns a Result that denotes the event was allowed and not cancelled.
         *
         * @return an allowed result
         * @since 2.0.0
         */
        public static Result allowed() {
            return ALLOWED;
        }

        /**
         * Returns a Result that denotes the event was denied and cancelled.
         *
         * @param reason the reason the event was denied
         * @return a denied result
         * @since 2.0.0
         */
        public static Result denied(final Component reason) {
            return new Result(true, reason);
        }

    }

}

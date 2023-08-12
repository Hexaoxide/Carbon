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
package net.draycia.carbon.common.event.events;

import java.util.List;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.event.CancellableImpl;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Event that's called when chat components are rendered for online players.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public class CarbonChatEventImpl extends CancellableImpl implements CarbonChatEvent {

    private final List<KeyedRenderer> renderers;
    private final CarbonPlayer sender;
    private final Component originalMessage;
    private final List<? extends Audience> recipients;
    private final @MonotonicNonNull ChatChannel chatChannel;
    private final @MonotonicNonNull SignedMessage signedMessage;
    public final boolean origin;
    private Component message;

    public CarbonChatEventImpl(
        final CarbonPlayer sender,
        final Component originalMessage,
        final List<? extends Audience> recipients,
        final List<KeyedRenderer> renderers,
        final @Nullable ChatChannel chatChannel,
        final @Nullable SignedMessage signedMessage
    ) {
        this(sender, originalMessage, recipients, renderers, chatChannel, signedMessage, true);
    }

    public CarbonChatEventImpl(
        final CarbonPlayer sender,
        final Component originalMessage,
        final List<? extends Audience> recipients,
        final List<KeyedRenderer> renderers,
        final @Nullable ChatChannel chatChannel,
        final @Nullable SignedMessage signedMessage,
        final boolean origin
    ) {
        this.sender = sender;
        this.originalMessage = originalMessage;
        this.message = originalMessage;
        this.recipients = recipients;
        this.renderers = renderers;
        this.chatChannel = chatChannel;
        this.signedMessage = signedMessage;
        this.origin = origin;
    }

    @Override
    public List<KeyedRenderer> renderers() {
        return this.renderers;
    }

    @Override
    public @MonotonicNonNull SignedMessage signedMessage() {
        return this.signedMessage;
    }

    @Override
    public CarbonPlayer sender() {
        return this.sender;
    }

    @Override
    public Component originalMessage() {
        return this.originalMessage;
    }

    @Override
    public Component message() {
        return this.message;
    }

    @Override
    public void message(final Component message) {
        this.message = message;
    }

    @Override
    public @MonotonicNonNull ChatChannel chatChannel() {
        return this.chatChannel;
    }

    @Override
    public List<? extends Audience> recipients() {
        return this.recipients;
    }

    public Component renderFor(final Audience viewer) {
        Component renderedMessage = this.message();
        for (final var renderer : this.renderers()) {
            renderedMessage = renderer.render(this.sender, viewer, renderedMessage, this.message());
        }
        return renderedMessage;
    }

}

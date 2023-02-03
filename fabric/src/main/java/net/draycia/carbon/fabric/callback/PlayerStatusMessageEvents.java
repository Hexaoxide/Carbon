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
package net.draycia.carbon.fabric.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PlayerStatusMessageEvents {

    public static final Event<MessageEventListener> JOIN_MESSAGE = EventFactory.createArrayBacked(
        MessageEventListener.class,
        callbacks -> messageEvent -> {
            for (final MessageEventListener callback : callbacks) {
                callback.onMessage(messageEvent);
            }
        }
    );

    public static final Event<MessageEventListener> QUIT_MESSAGE = EventFactory.createArrayBacked(
        MessageEventListener.class,
        callbacks -> messageEvent -> {
            for (final MessageEventListener callback : callbacks) {
                callback.onMessage(messageEvent);
            }
        }
    );

    public static final Event<MessageEventListener> DEATH_MESSAGE = EventFactory.createArrayBacked(
        MessageEventListener.class,
        callbacks -> messageEvent -> {
            for (final MessageEventListener callback : callbacks) {
                callback.onMessage(messageEvent);
            }
        }
    );

    private PlayerStatusMessageEvents() {
    }

    public interface MessageEvent {
        @Nullable Component message();

        void message(@Nullable Component message);

        default void disableMessage() {
            this.message(null);
        }

        ServerPlayer player();

        static MessageEvent of(final ServerPlayer player, final Component message) {
            return new MessageEventImpl(player, message);
        }
    }

    @FunctionalInterface
    public interface MessageEventListener {
        void onMessage(final MessageEvent event);
    }

    private static final class MessageEventImpl implements MessageEvent {
        private final ServerPlayer player;
        private @Nullable Component message;

        MessageEventImpl(final ServerPlayer player, final @Nullable Component message) {
            this.player = player;
            this.message = message;
        }

        @Override
        public @Nullable Component message() {
            return this.message;
        }

        @Override
        public void message(final @Nullable Component message) {
            this.message = message;
        }

        @Override
        public ServerPlayer player() {
            return this.player;
        }
    }

}

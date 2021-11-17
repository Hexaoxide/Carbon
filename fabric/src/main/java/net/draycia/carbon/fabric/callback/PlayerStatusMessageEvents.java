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

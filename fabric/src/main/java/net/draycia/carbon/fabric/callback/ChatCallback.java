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
package net.draycia.carbon.fabric.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.thread.NamedThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@DefaultQualifier(NonNull.class)
public enum ChatCallback {

    INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger();

    private @Nullable ExecutorService chatExecutor;
    private final List<Consumer<Chat>> listeners = new CopyOnWriteArrayList<>();

    private void initExecutor() {
        this.chatExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("CarbonChat-Chat-Thread"));
    }

    private void shutdownExecutor() {
        final @Nullable ExecutorService executor = this.chatExecutor;
        if (executor == null) {
            LOGGER.warn("Tried to shutdown null executor!");
            return;
        }
        this.chatExecutor = null;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(25L, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Timeout elapsed before tasks completed");
            }
        } catch (final InterruptedException | RuntimeException ex) {
            LOGGER.warn("Failed to shutdown chat executor", ex);
        }
    }

    public void registerListener(final Consumer<Chat> listener) {
        this.listeners.add(listener);
    }

    public void fireAsync(final MinecraftServer server, final ServerPlayer sender, final String chat) {
        final @Nullable ExecutorService executor = this.chatExecutor;
        if (executor != null) {
            this.chatExecutor.execute(() -> this.fire(server, sender, chat));
        } else {
            LOGGER.error("Tried to fire chat callback for player '{}' with message '{}' when executor was not initialized!", sender.getGameProfile().getName(), chat);
        }
    }

    private void fire(final MinecraftServer server, final ServerPlayer sender, final String chat) {
        // adventure-platform-fabric's PlayerListMixin makes this safe off main
        final ChatImpl c = new ChatImpl(sender, chat, server.getPlayerList().getPlayers());
        for (final Consumer<Chat> listener : this.listeners) {
            listener.accept(c);
        }
        if (c.cancelled) {
            return;
        }

        final MessageFormatter formatter = c.formatter == null ? defaultFormatter() : c.formatter;

        final @Nullable Component msgForConsole = formatter.format(sender, chat, server);
        if (msgForConsole != null) {
            server.sendMessage(msgForConsole);
        }

        for (final ServerPlayer player : c.recipients()) {
            final @Nullable Component rendered = formatter.format(sender, chat, player);
            if (rendered != null) {
                player.sendMessage(c.identity(), rendered, MessageType.CHAT);
            }
        }
    }

    public static void setup() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> INSTANCE.initExecutor());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> INSTANCE.shutdownExecutor());
    }

    private static MessageFormatter defaultFormatter() {
        return new MessageFormatter() {
            private @MonotonicNonNull Component rendered;

            @Override
            public Component format(final ServerPlayer sender, final String message, final Audience viewer) {
                if (this.rendered != null) {
                    return this.rendered;
                }
                this.rendered = translatable(
                    "chat.type.text",
                    sender.getDisplayName(),
                    text(message)
                );
                return this.rendered;
            }
        };
    }

    public interface Chat {

        ServerPlayer sender();

        Identity identity();

        void identity(final Identity identity);

        String message();

        void formatter(MessageFormatter formatter);

        void cancel();

        List<ServerPlayer> recipients();

    }

    @FunctionalInterface
    public interface MessageFormatter {

        @Nullable Component format(ServerPlayer sender, String message, Audience viewer);

    }

    private static final class ChatImpl implements Chat {

        private final ServerPlayer sender;
        private final String message;
        private Identity identity;
        private boolean cancelled = false;
        private @Nullable MessageFormatter formatter = null;
        private final List<ServerPlayer> recipients;

        private ChatImpl(final ServerPlayer sender, final String message, final List<ServerPlayer> players) {
            this.sender = sender;
            this.message = message;
            this.identity = Identity.identity(sender.getUUID());
            this.recipients = new ArrayList<>(players);
        }

        @Override
        public ServerPlayer sender() {
            return this.sender;
        }

        @Override
        public Identity identity() {
            return this.identity;
        }

        @Override
        public void identity(final Identity identity) {
            this.identity = identity;
        }

        @Override
        public String message() {
            return this.message;
        }

        @Override
        public void formatter(final MessageFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public List<ServerPlayer> recipients() {
            return this.recipients;
        }

    }

}

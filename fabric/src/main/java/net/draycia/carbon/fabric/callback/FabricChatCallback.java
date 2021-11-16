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
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
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

import static net.kyori.adventure.identity.Identity.identity;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@DefaultQualifier(NonNull.class)
public enum FabricChatCallback {

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
        final FabricServerAudiences fabricServerAudiences = FabricServerAudiences.of(server);

        // adventure-platform-fabric's PlayerListMixin makes this safe off main
        final ChatImpl c = new ChatImpl(sender, chat, server.getPlayerList().getPlayers());
        for (final Consumer<Chat> listener : this.listeners) {
            listener.accept(c);
        }
        if (c.cancelled) {
            return;
        }

        final Chat.MessageFormatter formatter = c.formatter == null ? defaultFormatter() : c.formatter;

        final Audience console = fabricServerAudiences.console();
        final @Nullable Component msgForConsole = formatter.format(sender, chat, console);
        if (msgForConsole != null) {
            console.sendMessage(msgForConsole);
        }

        for (final ServerPlayer player : c.recipients()) {
            final @Nullable Component rendered = formatter.format(sender, chat, fabricServerAudiences.audience(player));
            if (rendered != null) {
                fabricServerAudiences.audience(player).sendMessage(c.identity(), rendered, MessageType.CHAT);
            }
        }
    }

    public static void setup() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> INSTANCE.initExecutor());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> INSTANCE.shutdownExecutor());
    }

    private static Chat.MessageFormatter defaultFormatter() {
        return new Chat.MessageFormatter() {
            private @MonotonicNonNull Component rendered;

            @Override
            public Component format(final ServerPlayer sender, final String message, final Audience viewer) {
                if (this.rendered != null) {
                    return this.rendered;
                }
                this.rendered = translatable(
                    "chat.type.text",
                    FabricServerAudiences.of(sender.server).toAdventure(sender.getDisplayName()),
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

        interface MessageFormatter {

            @Nullable Component format(ServerPlayer sender, String message, Audience viewer);

        }

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

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
package net.draycia.carbon.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.CarbonShutdownEvent;
import net.draycia.carbon.common.CarbonChatInternal;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.config.MessagingSettings;
import net.draycia.carbon.common.listeners.PingHandler;
import net.draycia.carbon.common.messaging.packets.ChatMessagePacket;
import net.draycia.carbon.common.messaging.packets.SaveCompletedPacket;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.draycia.carbon.common.util.ExceptionLoggingScheduledThreadPoolExecutor;
import ninja.egg82.messenger.MessagingService;
import ninja.egg82.messenger.NATSMessagingService;
import ninja.egg82.messenger.PacketManager;
import ninja.egg82.messenger.RabbitMQMessagingService;
import ninja.egg82.messenger.RedisMessagingService;
import ninja.egg82.messenger.handler.AbstractServerMessagingHandler;
import ninja.egg82.messenger.handler.MessagingHandler;
import ninja.egg82.messenger.handler.MessagingHandlerImpl;
import ninja.egg82.messenger.packets.MultiPacket;
import ninja.egg82.messenger.packets.Packet;
import ninja.egg82.messenger.packets.server.InitializationPacket;
import ninja.egg82.messenger.packets.server.KeepAlivePacket;
import ninja.egg82.messenger.packets.server.PacketVersionPacket;
import ninja.egg82.messenger.packets.server.PacketVersionRequestPacket;
import ninja.egg82.messenger.packets.server.ShutdownPacket;
import ninja.egg82.messenger.services.PacketService;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@Singleton
@DefaultQualifier(NonNull.class)
public class MessagingManager {

    private static final byte protocolVersion = 0;

    private final CarbonChat carbonChat;
    private final @MonotonicNonNull ScheduledExecutorService executorService;
    private final @MonotonicNonNull PacketService packetService;
    private @MonotonicNonNull MessagingService messagingService;

    @Inject
    public MessagingManager(
        final ConfigFactory configFactory,
        final CarbonChat carbonChat,
        final Logger logger,
        final UserManagerInternal<?> userManager,
        final PingHandler pingHandler
    ) {
        if (!configFactory.primaryConfig().messagingSettings().enabled()) {
            if (!((CarbonChatInternal<?>) carbonChat).isProxy()) {
                logger.info("Messaging services disabled in config. Cross-server will not work without this!");
            }
            this.messagingService = EMPTY_MESSAGING_SERVICE;
            this.packetService = null;
            this.carbonChat = carbonChat;
            this.executorService = null;
            return;
        }

        PacketManager.register(MultiPacket.class, MultiPacket::new);
        PacketManager.register(KeepAlivePacket.class, KeepAlivePacket::new);
        PacketManager.register(InitializationPacket.class, InitializationPacket::new);
        PacketManager.register(PacketVersionPacket.class, PacketVersionPacket::new);
        PacketManager.register(PacketVersionRequestPacket.class, PacketVersionRequestPacket::new);
        PacketManager.register(ShutdownPacket.class, ShutdownPacket::new);
        //PacketManager.register(HeartbeatPacket.class, HeartbeatPacket::new);
        PacketManager.register(ChatMessagePacket.class, ChatMessagePacket::new);
        PacketManager.register(SaveCompletedPacket.class, SaveCompletedPacket::new);

        this.packetService = new PacketService(4, false, protocolVersion);
        this.executorService = new ExceptionLoggingScheduledThreadPoolExecutor(10,
            ConcurrentUtil.carbonThreadFactory(carbonChat.logger(), "MessagingManager"), carbonChat.logger());
        this.carbonChat = carbonChat;

        final MessagingHandlerImpl handlerImpl = new MessagingHandlerImpl(this.packetService);
        handlerImpl.addHandler(new CarbonServerHandler(carbonChat.serverId(), this.packetService, handlerImpl));
        handlerImpl.addHandler(new CarbonChatPacketHandler(this, userManager, pingHandler));

        try {
            this.initMessagingService(this.packetService, handlerImpl, new File("/"),
                configFactory.primaryConfig().messagingSettings());
        } catch (final IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        this.packetService.addMessenger(this.messagingService);

        this.packetService.queuePacket(new InitializationPacket(carbonChat.serverId(), protocolVersion));
        this.packetService.flushQueue();

        // Broadcast keepalive packets
        this.executorService.scheduleAtFixedRate(() -> {
            this.packetService.queuePacket(new KeepAlivePacket(carbonChat.serverId()));
            this.packetService.flushQueue();
        }, 5, 5, TimeUnit.SECONDS);

        this.executorService.scheduleAtFixedRate(() -> {
            try {
                this.packetService.flushQueue();
            } catch (final IndexOutOfBoundsException ignored) {

            }
        }, 0, 1, TimeUnit.SECONDS);

        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonShutdownEvent.class, 0, false, event -> {
            this.onShutdown();
        });
    }

    public @Nullable PacketService packetService() {
        return this.packetService;
    }

    public PacketService requirePacketService() {
        return Objects.requireNonNull(this.packetService, "packetService");
    }

    public void withPacketService(final Consumer<PacketService> consumer) {
        if (this.packetService != null) {
            consumer.accept(this.packetService);
        }
    }

    private void onShutdown() {
        if (this.executorService != null) {
            ConcurrentUtil.shutdownExecutor(this.executorService, TimeUnit.SECONDS, 10);
        }
        if (this.packetService != null) {
            this.packetService.flushQueue();
            this.packetService.shutdown();
        }
        this.messagingService.close();
    }

    private void initMessagingService(
        final PacketService packetService,
        final MessagingHandlerImpl handlerImpl,
        final File packetDir,
        final MessagingSettings messagingSettings
    ) throws IOException, TimeoutException, InterruptedException {
        final String name = "engine1";
        final String channelName = "carbon-data";

        switch (messagingSettings.brokerType()) {
            case RABBITMQ -> {
                this.carbonChat.logger().info("Initializing RabbitMQ Messaging services...");

                final RabbitMQMessagingService.Builder builder = RabbitMQMessagingService.builder(packetService, name, channelName, this.carbonChat.serverId(), handlerImpl, 0L, false, packetDir)
                    .url(messagingSettings.url(), messagingSettings.port(), messagingSettings.vhost())
                    .timeout(5000);

                if (messagingSettings.username() != null && !messagingSettings.username().isBlank()) {
                    builder.credentials(messagingSettings.username(), messagingSettings.password());
                }

                this.messagingService = builder.build();
            }
            case NATS -> {
                this.carbonChat.logger().info("Initializing NATS Messaging services...");

                final NATSMessagingService.Builder builder = NATSMessagingService.builder(packetService, name, channelName, this.carbonChat.serverId(), handlerImpl, 0L, false, packetDir)
                    .url(messagingSettings.url(), messagingSettings.port())
                    .life(5000);

                if (messagingSettings.credentialsFile() != null && !messagingSettings.credentialsFile().isBlank()) {
                    builder.credentials(messagingSettings.credentialsFile());
                }

                this.messagingService = builder.build();
            }
            case REDIS -> {
                this.carbonChat.logger().info("Initializing Redis Messaging services...");

                final RedisMessagingService.Builder builder = RedisMessagingService.builder(packetService, name, channelName, this.carbonChat.serverId(), handlerImpl, 0L, false, packetDir)
                    .url(messagingSettings.url(), messagingSettings.port());

                if (messagingSettings.password() != null && !messagingSettings.password().isBlank()) {
                    builder.credentials(messagingSettings.password());
                }

                this.messagingService = builder.build();
            }
            case NONE ->
                throw new IllegalStateException("MessagingManager initialized with no messaging broker selected!");
        }
    }

    public enum BrokerType {
        NONE,
        RABBITMQ,
        NATS,
        REDIS,
    }

    private static final class CarbonServerHandler extends AbstractServerMessagingHandler {

        private CarbonServerHandler(
            final @NotNull UUID serverId,
            final @NotNull PacketService packetService,
            final @NotNull MessagingHandler messagingHandler
        ) {
            super(serverId, packetService, messagingHandler);
        }

    }

    private static final MessagingService EMPTY_MESSAGING_SERVICE = new EmptyMessagingService();

    private static final class EmptyMessagingService implements MessagingService {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void close() {

        }

        @Override
        public boolean isClosed() {
            return true;
        }

        @Override
        public void sendPacket(final @NotNull UUID messageId, final @NotNull Packet packet) {

        }

    }

}

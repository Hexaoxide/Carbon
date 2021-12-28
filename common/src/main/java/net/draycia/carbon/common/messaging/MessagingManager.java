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
package net.draycia.carbon.common.messaging;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.CarbonShutdownEvent;
import net.draycia.carbon.common.config.MessagingSettings;
import ninja.egg82.messenger.MessagingService;
import ninja.egg82.messenger.NATSMessagingService;
import ninja.egg82.messenger.RabbitMQMessagingService;
import ninja.egg82.messenger.RedisMessagingService;
import ninja.egg82.messenger.handler.AbstractMessagingHandler;
import ninja.egg82.messenger.handler.AbstractServerMessagingHandler;
import ninja.egg82.messenger.handler.MessagingHandler;
import ninja.egg82.messenger.handler.MessagingHandlerImpl;
import ninja.egg82.messenger.packets.Packet;
import ninja.egg82.messenger.packets.server.InitializationPacket;
import ninja.egg82.messenger.packets.server.KeepAlivePacket;
import ninja.egg82.messenger.services.PacketService;
import org.jetbrains.annotations.NotNull;

public class MessagingManager {

    private final UUID serverId = UUID.randomUUID();
    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);

    private PacketService packetService;
    private MessagingService messagingService;

    public void init(final MessagingSettings messagingSettings) {
        this.packetService = new PacketService(4, false, (byte) 1);

        final MessagingHandlerImpl handlerImpl = new MessagingHandlerImpl(packetService);
        handlerImpl.addHandler(new CarbonServerHandler(serverId, packetService, handlerImpl));
        handlerImpl.addHandler(new CarbonPacketHandler(packetService));

        try {
            this.initMessagingService(packetService, handlerImpl, new File("/"), messagingSettings);
        } catch (final IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        packetService.queuePacket(new InitializationPacket(this.serverId, (byte) 1));

        executorService.scheduleAtFixedRate(() -> {
            packetService.queuePacket(new KeepAlivePacket(this.serverId));
            packetService.flushQueue();
        }, 1, 15, TimeUnit.SECONDS);

        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonShutdownEvent.class, 0, false, event -> {
            this.onShutdown();
        });

        packetService.flushQueue();
    }

    public PacketService packetService() {
        return this.packetService;
    }

    private void onShutdown() {
        try {
            this.executorService.awaitTermination(15, TimeUnit.SECONDS);
        } catch (final InterruptedException ignored) {

        }
        this.packetService.flushQueue();
        this.packetService.shutdown();
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

        this.messagingService = switch (messagingSettings.brokerType()) {
            case RABBITMQ -> RabbitMQMessagingService.builder(packetService, name, channelName, this.serverId, handlerImpl, 0L, false, packetDir)
                .url(messagingSettings.url(), messagingSettings.port(), messagingSettings.vhost())
                .credentials(messagingSettings.username(), messagingSettings.password())
                .timeout(5000)
                .build();
            case NATS -> NATSMessagingService.builder(packetService, name, channelName, this.serverId, handlerImpl, 0L, false, packetDir)
                .url(messagingSettings.url(), messagingSettings.port())
                .credentials(messagingSettings.credentialsFile())
                .life(5000)
                .build();
            case REDIS -> RedisMessagingService.builder(packetService, name, channelName, this.serverId, handlerImpl, 0L, false, packetDir)
                .url(messagingSettings.url(), messagingSettings.port())
                .credentials(messagingSettings.password())
                .life(5000, 5000)
                .build();
            case NONE -> throw new IllegalStateException("MessagingManager initialized with no messaging broker selected!");
        };
    }

    public enum BrokerType {
        NONE,
        RABBITMQ,
        NATS,
        REDIS,
    }

    private static final class CarbonPacketHandler extends AbstractMessagingHandler {

        private CarbonPacketHandler(@NotNull PacketService packetService) {
            super(packetService);
        }

        @Override
        protected boolean handlePacket(@NotNull Packet packet) {
            if (packet instanceof ChatMessagePacket messagePacket) {

            }

            return false;
        }

    }

    private static final class CarbonServerHandler extends AbstractServerMessagingHandler {

        private CarbonServerHandler(@NotNull UUID serverId, @NotNull PacketService packetService, @NotNull MessagingHandler messagingHandler) {
            super(serverId, packetService, messagingHandler);
        }

    }

}

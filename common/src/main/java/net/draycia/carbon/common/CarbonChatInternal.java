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
package net.draycia.carbon.common;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.commands.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.listeners.Listener;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.ProfileCache;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.draycia.carbon.common.util.PlayerUtils;
import ninja.egg82.messenger.services.PacketService;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class CarbonChatInternal<C extends CarbonPlayer> implements CarbonChat {

    private final UUID serverId;
    private final Injector injector;
    private final Logger logger;
    private final ScheduledExecutorService periodicTasks;
    private final ProfileCache profileCache;
    private final ProfileResolver profileResolver;
    private final UserManagerInternal<C> userManager;
    private final ExecutionCoordinatorHolder commandExecutor;
    private final CarbonServer carbonServer;
    private final CarbonMessages carbonMessages;
    private final CarbonEventHandler eventHandler;
    private final CarbonChannelRegistry channelRegistry;
    private final Provider<MessagingManager> messagingManager;

    protected CarbonChatInternal(
        final Injector injector,
        final Logger logger,
        final ScheduledExecutorService periodicTasks,
        final ProfileCache profileCache,
        final ProfileResolver profileResolver,
        final UserManagerInternal<C> userManager,
        final ExecutionCoordinatorHolder commandExecutor,
        final CarbonServer carbonServer,
        final CarbonMessages carbonMessages,
        final CarbonEventHandler eventHandler,
        final CarbonChannelRegistry channelRegistry,
        final Provider<MessagingManager> messagingManagerProvider
    ) {
        this.serverId = UUID.randomUUID();
        this.injector = injector;
        this.logger = logger;
        this.periodicTasks = periodicTasks;
        this.profileCache = profileCache;
        this.profileResolver = profileResolver;
        this.userManager = userManager;
        this.commandExecutor = commandExecutor;
        this.carbonServer = carbonServer;
        this.carbonMessages = carbonMessages;
        this.eventHandler = eventHandler;
        this.channelRegistry = channelRegistry;
        this.messagingManager = messagingManagerProvider;
    }

    protected void init() {
        // Listeners
        final Set<Listener> listeners = this.injector.getInstance(Key.get(new TypeLiteral<Set<Listener>>() {}));

        // Commands
        // This is a bit awkward looking
        CloudUtils.loadCommands(this.injector);
        final var commandSettings = CloudUtils.loadCommandSettings(this.injector);
        CloudUtils.registerCommands(commandSettings);

        this.periodicTasks.scheduleAtFixedRate(
            () -> PlayerUtils.saveLoggedInPlayers(this.carbonServer, this.userManager, this.logger),
            5,
            5,
            TimeUnit.MINUTES
        );
        this.periodicTasks.scheduleAtFixedRate(
            this.profileCache::save,
            15,
            15,
            TimeUnit.MINUTES
        );
        this.periodicTasks.scheduleAtFixedRate(
            this.userManager::cleanup,
            30,
            30,
            TimeUnit.SECONDS
        );

        // Load channels
        this.channelRegistry().loadConfigChannels(this.carbonMessages);
    }

    protected void shutdown() {
        this.messagingManager.get().withPacketService(packetService -> {
            packetService.queuePacket(this.injector.getInstance(PacketFactory.class).clearLocalPlayersPacket());
        });
        this.messagingManager.get().onShutdown();
        ConcurrentUtil.shutdownExecutor(this.periodicTasks, TimeUnit.MILLISECONDS, 500);
        this.profileCache.save();
        this.profileResolver.shutdown();
        this.userManager.shutdown();
        this.commandExecutor.shutdown();
    }

    @Override
    public UUID serverId() {
        return this.serverId;
    }

    public Logger logger() {
        return this.logger;
    }

    @Override
    public CarbonServer server() {
        return this.carbonServer;
    }

    @Override
    public UserManagerInternal<C> userManager() {
        return this.userManager;
    }

    @Override
    public CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

    @Override
    public CarbonChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    public @Nullable PacketService packetService() {
        return this.messagingManager.get().packetService();
    }

    public boolean isProxy() {
        return false;
    }

    public Injector injector() {
        return this.injector;
    }

}

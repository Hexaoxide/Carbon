/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.velocity;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.common.CarbonChatInternal;
import net.draycia.carbon.common.PeriodicTasks;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.ExecutionCoordinatorHolder;
import net.draycia.carbon.common.integration.miniplaceholders.MiniPlaceholdersExpansion;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.users.PlatformUserManager;
import net.draycia.carbon.common.users.ProfileCache;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.velocity.listeners.VelocityListener;
import org.apache.logging.log4j.LogManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public class CarbonChatVelocity extends CarbonChatInternal {

    private final ProxyServer proxyServer;

    @Inject
    public CarbonChatVelocity(
        final ProxyServer proxyServer,
        final Injector injector,
        final PluginContainer pluginContainer,
        @PeriodicTasks final ScheduledExecutorService periodicTasks,
        final ProfileCache profileCache,
        final ProfileResolver profileResolver,
        final ExecutionCoordinatorHolder commandExecutor,
        final CarbonMessages carbonMessages,
        final PlatformUserManager userManager,
        final CarbonServerVelocity carbonServer,
        final CarbonEventHandler eventHandler,
        final CarbonChannelRegistry channelRegistry,
        final Provider<MessagingManager> messagingManager
    ) {
        super(
            injector,
            LogManager.getLogger(pluginContainer.getDescription().getId()),
            periodicTasks,
            profileCache,
            profileResolver,
            userManager,
            commandExecutor,
            carbonServer,
            carbonMessages,
            eventHandler,
            channelRegistry,
            messagingManager
        );
        this.proxyServer = proxyServer;

        CarbonChatProvider.register(this);
    }

    public void onInitialization(final CarbonVelocityBootstrap carbonVelocityBootstrap) {
        this.init();

        final Set<VelocityListener<?>> listeners = this.injector().getInstance(Key.get(new TypeLiteral<Set<VelocityListener<?>>>() {}));
        for (final VelocityListener<?> listener : listeners) {
            listener.register(this.proxyServer.getEventManager(), carbonVelocityBootstrap);
        }

        MiniPlaceholdersExpansion.register(this.injector());

        this.checkVersion();
    }

    public void onShutdown() {
        this.shutdown();
    }

    @Override
    public boolean isProxy() {
        return true;
    }

}

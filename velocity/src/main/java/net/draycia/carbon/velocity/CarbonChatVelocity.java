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
package net.draycia.carbon.velocity;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import java.util.Set;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.util.CloudUtils;
import net.draycia.carbon.common.util.ListenerUtils;
import net.draycia.carbon.velocity.listeners.VelocityChatListener;
import net.draycia.carbon.velocity.listeners.VelocityPlayerJoinListener;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.moonshine.message.IMessageRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Plugin(
    id = "$[ID]",
    name = "$[NAME]",
    version = "$[VERSION]",
    description = "$[DESCRIPTION]",
    url = "$[URL]",
    authors = {"Draycia"},
    dependencies = {@Dependency(id = "luckperms")}
)
@DefaultQualifier(NonNull.class)
public class CarbonChatVelocity implements CarbonChat {

    private static final Set<Class<?>> LISTENER_CLASSES = Set.of(
        VelocityChatListener.class,
        VelocityPlayerJoinListener.class
    );

    private final Path dataDirectory;
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Injector injector;

    private final CarbonMessages carbonMessages;
    private final ChannelRegistry channelRegistry;
    private final CarbonServerVelocity carbonServer;
    private final CarbonEventHandler eventHandler = new CarbonEventHandler();

    @Inject
    public CarbonChatVelocity(
        @DataDirectory final Path dataDirectory,
        final ProxyServer proxyServer,
        final PluginContainer pluginContainer,
        final Injector injector
    ) {
        CarbonChatProvider.register(this);

        this.dataDirectory = dataDirectory;
        this.proxyServer = proxyServer;
        this.logger = LogManager.getLogger(pluginContainer.getDescription().getId());

        final CarbonChatVelocityModule carbonVelocityModule;

        carbonVelocityModule = new CarbonChatVelocityModule(
            this.logger, this.dataDirectory, pluginContainer, this.proxyServer, this);

        this.injector = injector.createChildInjector(carbonVelocityModule);

        this.carbonMessages = this.injector.getInstance(CarbonMessages.class);
        this.channelRegistry = this.injector.getInstance(ChannelRegistry.class);
        this.carbonServer = this.injector.getInstance(CarbonServerVelocity.class);
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        for (final Class<?> clazz : LISTENER_CLASSES) {
            this.proxyServer.getEventManager().register(this, this.injector.getInstance(clazz));
        }

        // Listeners
        ListenerUtils.registerCommonListeners(this.injector);

        // Load channels
        ((CarbonChannelRegistry) this.channelRegistry()).loadConfigChannels(this.messageService);

        // Commands
        CloudUtils.loadCommands(this.injector);
        final var commandSettings = CloudUtils.loadCommandSettings(this.injector);
        CloudUtils.registerCommands(commandSettings);
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path dataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public CarbonServerVelocity server() {
        return this.carbonServer;
    }

    @Override
    public ChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    @Override
    public <T extends Audience> IMessageRenderer<T, String, RenderedMessage, Component> messageRenderer() {
        return this.injector.getInstance(VelocityMessageRenderer.class);
    }

    public CarbonMessages carbonMessages() {
        return this.carbonMessages;
    }

    @Override
    public final @NonNull CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

}

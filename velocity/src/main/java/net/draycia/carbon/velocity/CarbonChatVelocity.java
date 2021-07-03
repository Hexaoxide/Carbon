package net.draycia.carbon.velocity;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.common.CarbonChatCommon;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.velocity.listeners.VelocityChatListener;
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
    authors = {"Draycia"}
)
@DefaultQualifier(NonNull.class)
public class CarbonChatVelocity extends CarbonChatCommon {

    private static final Set<Class<?>> LISTENER_CLASSES = Set.of(
        VelocityChatListener.class
    );

    private final Path dataDirectory;
    private final ProxyServer proxyServer;
    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Injector injector;

    private final CarbonMessageService messageService;
    private final ChannelRegistry channelRegistry;
    private final CarbonServerVelocity carbonServer;

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
        this.pluginContainer = pluginContainer;
        this.logger = LogManager.getLogger(this.pluginContainer.getDescription().getId());

        final CarbonChatVelocityModule carbonVelocityModule;

        try {
            carbonVelocityModule = new CarbonChatVelocityModule(
                this.logger, this.dataDirectory, this.pluginContainer, this.proxyServer, this);
        } catch (final URISyntaxException ex) {
            throw new RuntimeException(ex);
        }

        this.injector = injector.createChildInjector(carbonVelocityModule);

        this.messageService = injector.getInstance(CarbonMessageService.class);
        this.channelRegistry = injector.getInstance(ChannelRegistry.class);
        this.carbonServer = injector.getInstance(CarbonServerVelocity.class);
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        super.initialize(this.injector);

        for (final Class<?> clazz : LISTENER_CLASSES) {
            this.proxyServer.getEventManager().register(this, this.injector.getInstance(clazz));
        }

        ((CarbonChannelRegistry) this.channelRegistry()).loadChannels();
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

    public CarbonMessageService messageService() {
        return this.messageService;
    }

    private final CarbonEventHandler eventHandler = new CarbonEventHandler();

    @Override
    public final @NonNull CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

}

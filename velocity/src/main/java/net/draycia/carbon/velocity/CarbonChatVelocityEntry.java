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
public class CarbonChatVelocityEntry {

    private static final Set<Class<?>> LISTENER_CLASSES = Set.of(
        VelocityChatListener.class
    );

    private final Path dataDirectory;
    private final ProxyServer server;
    private final PluginContainer pluginContainer;
    private final CarbonChatVelocity carbon;
    private final Injector injector;
    private final Logger logger;

    @Inject
    public CarbonChatVelocityEntry(
        @DataDirectory final Path dataDirectory,
        final ProxyServer server,
        final PluginContainer pluginContainer,
        final Injector injector
    ) {
        this.dataDirectory = dataDirectory;
        this.server = server;
        this.pluginContainer = pluginContainer;
        this.logger = LogManager.getLogger(this.pluginContainer.getDescription().getId());

        final CarbonChatVelocityModule carbonVelocityModule;
        try {
            carbonVelocityModule = new CarbonChatVelocityModule(this.logger, this.dataDirectory);
        } catch (final URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        this.injector = injector.createChildInjector(carbonVelocityModule);

        this.carbon = this.injector.getInstance(CarbonChatVelocity.class);
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        this.carbon.initialize();
        for (final Class<?> clazz : LISTENER_CLASSES) {
            this.server.getEventManager().register(this, this.injector.getInstance(clazz));
        }
    }
}

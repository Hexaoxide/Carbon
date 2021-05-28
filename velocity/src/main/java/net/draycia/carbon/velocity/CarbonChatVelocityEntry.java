package net.draycia.carbon.velocity;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import java.net.URISyntaxException;
import java.nio.file.Path;
import net.draycia.carbon.velocity.listeners.VelocityChatListener;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
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

    private final Logger logger;
    private final Path dataDirectory;
    private final ProxyServer server;
    private final PluginContainer pluginContainer;

    private final @MonotonicNonNull CarbonChatVelocity carbon;
    private @MonotonicNonNull Injector injector;

    @Inject
    public CarbonChatVelocityEntry(
        final Logger logger,
        final Path dataDirectory,
        final ProxyServer server,
        final PluginContainer pluginContainer
    ) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.server = server;
        this.pluginContainer = pluginContainer;

        try {
            this.injector = Guice.createInjector(new CarbonChatVelocityModule(
                logger,
                this,
                this.dataDirectory
            ));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        this.carbon = this.injector.getInstance(CarbonChatVelocity.class);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.server.getEventManager().register(this,
            this.injector.getInstance(VelocityChatListener.class));
    }

    public Path dataDirectory() {
        return this.dataDirectory;
    }

    public ProxyServer proxyServer() {
        return this.server;
    }

    public PluginContainer pluginContainer() {
        return this.pluginContainer;
    }

}

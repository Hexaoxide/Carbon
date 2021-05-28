package net.draycia.carbon.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
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

    private final Logger logger;
    private final Path dataDirectory;
    private final ProxyServer server;
    private final PluginContainer pluginContainer;

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

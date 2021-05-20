package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.draycia.carbon.sponge.listeners.SpongeChatListener;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.nio.file.Path;

@Plugin("carbonchat")
@DefaultQualifier(NonNull.class)
public final class CarbonChatSpongeEntry {

    private static final int BSTATS_PLUGIN_ID = 11279;

    private @MonotonicNonNull CarbonChatSponge carbon;
    private final Injector injector;

    private final PluginContainer pluginContainer;

    @Inject
    public CarbonChatSpongeEntry(
        //final Metrics.Factory metricsFactory,
        final PluginContainer pluginContainer,
        final Logger logger,
        final @ConfigDir(sharedRoot = false) Path dataDirectory,
        final Injector injector
    ) {
        this.pluginContainer = pluginContainer;

        this.carbon = new CarbonChatSponge();

        this.injector = injector.createChildInjector(
            new CarbonChatSpongeModule(logger, this.carbon, this, dataDirectory));

        //metricsFactory.make(BSTATS_PLUGIN_ID);
    }

    @Listener
    public void onInitialize(final StartingEngineEvent<Server> event) {
        Sponge.eventManager().registerListeners(pluginContainer,
            this.injector.getInstance(SpongeChatListener.class));

        this.carbon.initialize();
    }

}

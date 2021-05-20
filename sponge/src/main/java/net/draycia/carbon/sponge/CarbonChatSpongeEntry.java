package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.draycia.carbon.sponge.listeners.SpongeChatListener;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("carbonchat")
@DefaultQualifier(NonNull.class)
public final class CarbonChatSpongeEntry {

    private static final int BSTATS_PLUGIN_ID = 11279;

    private @MonotonicNonNull CarbonChatSponge carbon;
    private final Game game;
    private final Injector injector;

    private final PluginContainer pluginContainer;

    @Inject
    public CarbonChatSpongeEntry(
        //final Metrics.Factory metricsFactory,
        final Game game,
        final PluginContainer pluginContainer,
        final Injector injector
    ) {
        this.game = game;
        this.pluginContainer = pluginContainer;

        this.injector = injector.createChildInjector(new CarbonChatSpongeModule());
        this.carbon = this.injector.getInstance(CarbonChatSponge.class);

        //metricsFactory.make(BSTATS_PLUGIN_ID);
    }

    @Listener
    public void onInitialize(final StartingEngineEvent<Server> event) {
        this.game.eventManager().registerListeners(this.pluginContainer,
            this.injector.getInstance(SpongeChatListener.class));

        this.carbon.initialize();
    }

}

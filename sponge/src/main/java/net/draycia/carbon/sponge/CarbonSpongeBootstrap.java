package net.draycia.carbon.sponge;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChatProvider;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;

public class CarbonSpongeBootstrap {

    private @MonotonicNonNull CarbonChatSponge carbonChat;
    private final PluginContainer pluginContainer;
    private final Path dataDirectory;
    private final Injector parentInjector;
    private @MonotonicNonNull Injector injector;

    @Inject
    public CarbonSpongeBootstrap(
        final Injector injector,
        final PluginContainer pluginContainer,
        @ConfigDir(sharedRoot = false) final Path dataDirectory
    ) {
        this.parentInjector = injector;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
    }

    @Listener
    public void onInitialize(final StartingEngineEvent<Server> event) {
        // TODO: move somewhere earlier like ctor? other modules have these two lines under onLoad
        this.injector = this.parentInjector.createChildInjector(new CarbonChatSpongeModule(this.dataDirectory, this.pluginContainer));
        this.carbonChat = this.injector.getInstance(CarbonChatSponge.class);

        this.carbonChat.onInitialize(event);
        CarbonChatProvider.register(this.carbonChat);
    }

    @Listener
    public void onDisable(final StoppingEngineEvent<Server> event) {
        this.carbonChat.onDisable(event);
    }

}

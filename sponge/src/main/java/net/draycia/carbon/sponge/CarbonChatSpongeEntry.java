package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.draycia.carbon.sponge.listeners.SpongeChatListener;
import net.draycia.carbon.sponge.users.CarbonPlayerSponge;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("carbonchat")
@DefaultQualifier(NonNull.class)
public final class CarbonChatSpongeEntry {

    private static final Set<Class<?>> LISTENER_CLASSES = Set.of(
        SpongeChatListener.class
    );
    private static final int BSTATS_PLUGIN_ID = 11279;

    private final @MonotonicNonNull CarbonChatSponge carbon;
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

        this.injector = injector.createChildInjector(injector.getInstance(CarbonChatSpongeModule.class));
        this.carbon = this.injector.getInstance(CarbonChatSponge.class);

        for (final Class<?> clazz : LISTENER_CLASSES) {
            this.game.eventManager().registerListeners(this.pluginContainer, this.injector.getInstance(clazz));
        }
        //metricsFactory.make(BSTATS_PLUGIN_ID);

        this.carbon.initialize();
    }

    @Listener
    public void onInitialize(final StartingEngineEvent<Server> event) {
        Sponge.asyncScheduler().submit(Task.builder()
            .interval(5, TimeUnit.MINUTES)
            .plugin(this.pluginContainer)
            .execute(this::savePlayers)
            .build());
    }

    @Listener
    public void onDisable(final StoppingEngineEvent<Server> event) {
        this.savePlayers();
    }

    private void savePlayers() {
        for (final var player : this.carbon.server().players()) {
            this.carbon.userManager().savePlayer(((CarbonPlayerSponge)player).carbonPlayer()).thenAccept(result -> {
                if (result.player() == null) {
                    this.carbon.server().console().sendMessage(result.reason());
                }
            });
        }
    }

}

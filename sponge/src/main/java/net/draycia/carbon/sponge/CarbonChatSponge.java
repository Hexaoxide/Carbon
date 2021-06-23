package net.draycia.carbon.sponge;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge.SpongeCommandManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.CarbonChatCommon;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.sponge.command.SpongeCommander;
import net.draycia.carbon.sponge.command.SpongePlayerCommander;
import net.draycia.carbon.sponge.listeners.SpongeChatListener;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("carbonchat")
@DefaultQualifier(NonNull.class)
public final class CarbonChatSponge extends CarbonChatCommon {

    private static final Set<Class<?>> LISTENER_CLASSES = Set.of(
        SpongeChatListener.class
    );
    private static final int BSTATS_PLUGIN_ID = 11279;

    private final CarbonMessageService messageService;
    private final CarbonServerSponge carbonServerSponge;
    private final ChannelRegistry channelRegistry;
    private final Game game;
    private final Injector injector;
    private final Logger logger;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;
    private final UserManager userManager;

    @Inject
    public CarbonChatSponge(
        //final Metrics.Factory metricsFactory,
        final Game game,
        final PluginContainer pluginContainer,
        final Injector injector,
        final Logger logger,
        @ConfigDir(sharedRoot = false) final Path dataDirectory
    ) {
        CarbonChatProvider.register(this);

        this.game = game;
        this.pluginContainer = pluginContainer;

        this.injector = injector.createChildInjector(new CarbonChatSpongeModule(
            this, dataDirectory, pluginContainer));

        this.logger = logger;
        this.messageService = this.injector.getInstance(CarbonMessageService.class);
        this.channelRegistry = this.injector.getInstance(ChannelRegistry.class);
        this.carbonServerSponge = this.injector.getInstance(CarbonServerSponge.class);
        this.userManager = this.injector.getInstance(UserManager.class);
        this.dataDirectory = dataDirectory;

        for (final Class<?> clazz : LISTENER_CLASSES) {
            this.game.eventManager().registerListeners(this.pluginContainer, this.injector.getInstance(clazz));
        }
        //metricsFactory.make(BSTATS_PLUGIN_ID);

        this.initialize();
    }

    @Listener
    public void onInitialize(final StartingEngineEvent<Server> event) {
        Sponge.asyncScheduler().submit(Task.builder()
            .interval(5, TimeUnit.MINUTES)
            .plugin(this.pluginContainer)
            .execute(this::savePlayers)
            .build());

        ((CarbonChannelRegistry) this.channelRegistry()).loadChannels();
    }

    @Listener
    public void onDisable(final StoppingEngineEvent<Server> event) {
        this.savePlayers();
    }

    private void savePlayers() {
        for (final var player : this.server().players()) {
            this.userManager().savePlayer(player.carbonPlayer()).thenAccept(result -> {
                if (result.player() == null) {
                    this.server().console().sendMessage(result.reason());
                }
            });
        }
    }

    public UserManager userManager() {
        return this.userManager;
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
    public CarbonServerSponge server() {
        return this.carbonServerSponge;
    }

    @Override
    public ChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    @Override
    public CarbonMessageService messageService() {
        return this.messageService;
    }

    @Override
    protected CommandManager<Commander> createCommandManager() {
        final SpongeCommandManager<Commander> commandManager = new SpongeCommandManager<>(
            this.pluginContainer,
            AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
            commander -> ((SpongeCommander) commander).commandCause(),
            commandCause -> {
                if (commandCause.subject() instanceof ServerPlayer player) {
                    return new SpongePlayerCommander(this, player, commandCause);
                }
                return SpongeCommander.from(commandCause);
            }
        );
        commandManager.parserMapper().cloudNumberSuggestions(true);
        return commandManager;
    }

}

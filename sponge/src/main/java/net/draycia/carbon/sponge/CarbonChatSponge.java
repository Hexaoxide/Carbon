package net.draycia.carbon.sponge;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge.SpongeCommandManager;
import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.CarbonChatCommon;
import net.draycia.carbon.common.Injector;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.sponge.command.SpongeCommander;
import net.draycia.carbon.sponge.command.SpongePlayerCommander;
import net.draycia.carbon.sponge.listeners.SpongeChatListener;
import net.draycia.carbon.sponge.users.MemoryUserManagerSponge;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.nio.file.Path;

@Plugin("carbonchat")
@DefaultQualifier(NonNull.class)
public final class CarbonChatSponge extends CarbonChatCommon {

    private static final int BSTATS_PLUGIN_ID = 11279;

    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Path dataDirectory;

    private final CarbonServerSponge carbonServerSponge;
    private final UserManager userManager = new MemoryUserManagerSponge();

    @Inject
    public CarbonChatSponge(
        //final Metrics.Factory metricsFactory,
        final PluginContainer pluginContainer,
        final Logger logger,
        final Path dataDirectory
    ) {
        this.pluginContainer = pluginContainer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.carbonServerSponge = new CarbonServerSponge(this);

        Injector.provide(CarbonChat.class, this);

        Sponge.eventManager().registerListeners(pluginContainer, new SpongeChatListener());

        this.initialize();

        //metricsFactory.make(BSTATS_PLUGIN_ID);
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
    public CarbonServer server() {
        return this.carbonServerSponge;
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

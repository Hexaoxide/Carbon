package net.draycia.carbon.velocity;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.net.URISyntaxException;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.common.CarbonCommonModule;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.velocity.command.VelocityCommander;
import net.draycia.carbon.velocity.command.VelocityPlayerCommander;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonChatVelocityModule extends AbstractModule {

    private final Logger logger;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;
    private final ProxyServer proxyServer;
    private final CarbonChat carbonChat;

    CarbonChatVelocityModule(
        final Logger logger,
        final Path dataDirectory,
        final PluginContainer pluginContainer,
        final ProxyServer proxyServer,
        final CarbonChat carbonChat
    ) throws URISyntaxException {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.pluginContainer = pluginContainer;
        this.proxyServer = proxyServer;
        this.carbonChat = carbonChat;
    }

    @Provides
    @Singleton
    protected @NonNull CommandManager<Commander> createCommandManager() {
        final VelocityCommandManager<Commander> commandManager = new VelocityCommandManager<>(
            this.pluginContainer,
            this.proxyServer,
            AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
            commandSender -> {
                if (commandSender instanceof Player player) {
                    return new VelocityPlayerCommander(this.carbonChat, player);
                }
                return VelocityCommander.from(commandSender);
            },
            commander -> ((VelocityCommander) commander).commandSource()
        );
        final var brigadierManager = commandManager.brigadierManager();
        brigadierManager.setNativeNumberSuggestions(false);
        return commandManager;
    }

    @Override
    public void configure() {
        this.install(new CarbonCommonModule());

        this.bind(CarbonChat.class).to(CarbonChatVelocity.class);
        this.bind(Logger.class).toInstance(this.logger);
        this.bind(Path.class).annotatedWith(ForCarbon.class).toInstance(this.dataDirectory);
        this.bind(CarbonServer.class).to(CarbonServerVelocity.class);
    }

}

package net.draycia.carbon.velocity;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.common.CarbonChatCommon;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.velocity.command.VelocityCommander;
import net.draycia.carbon.velocity.command.VelocityPlayerCommander;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonChatVelocity extends CarbonChatCommon {

    private final Logger logger;
    private final CarbonServer server;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;
    private final ProxyServer proxyServer;
    private final CarbonMessageService messageService;
    private final ChannelRegistry channelRegistry;

    @Inject
    public CarbonChatVelocity(
        final Logger logger,
        final CarbonServer server,
        @ForCarbon final Path dataDirectory,
        final CarbonMessageService messageService,
        final PluginContainer pluginContainer,
        final ProxyServer proxyServer,
        final ChannelRegistry channelRegistry
    ) {
        this.logger = logger;
        this.server = server;
        this.messageService = messageService;
        this.dataDirectory = dataDirectory;
        this.pluginContainer = pluginContainer;
        this.proxyServer = proxyServer;
        this.channelRegistry = channelRegistry;

        CarbonChatProvider.register(this);
        ((CarbonChannelRegistry) this.channelRegistry()).loadChannels();
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
        return this.server;
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
    protected @NonNull CommandManager<Commander> createCommandManager() {
        final VelocityCommandManager<Commander> commandManager = new VelocityCommandManager<>(
            this.pluginContainer,
            this.proxyServer,
            AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
            commandSender -> {
                if (commandSender instanceof Player player) {
                    return new VelocityPlayerCommander(this, player);
                }
                return VelocityCommander.from(commandSender);
            },
            commander -> ((VelocityCommander) commander).commandSource()
        );
        final var brigadierManager = commandManager.brigadierManager();
        brigadierManager.setNativeNumberSuggestions(false);
        return commandManager;
    }

}

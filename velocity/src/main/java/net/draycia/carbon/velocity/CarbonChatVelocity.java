package net.draycia.carbon.velocity;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.proxy.Player;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.common.CarbonChatCommon;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.velocity.command.VelocityCommander;
import net.draycia.carbon.velocity.command.VelocityPlayerCommander;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

@DefaultQualifier(NonNull.class)
public class CarbonChatVelocity extends CarbonChatCommon {

    private final Logger logger;
    private final CarbonChatVelocityEntry plugin;
    private final CarbonServer server;
    private final Path dataDirectory;

    @Inject
    public CarbonChatVelocity(
        final Logger logger,
        final CarbonChatVelocityEntry plugin,
        final CarbonServer server,
        final Path dataDirectory,
        final CarbonMessageService messageService
    ) {
        super(messageService);
        this.logger = logger;
        this.plugin = plugin;
        this.server = server;
        this.dataDirectory = dataDirectory;
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
    protected @NonNull CommandManager<Commander> createCommandManager() {
        final VelocityCommandManager<Commander> commandManager;
        commandManager = new VelocityCommandManager<>(
            this.plugin.pluginContainer(),
            this.plugin.proxyServer(),
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

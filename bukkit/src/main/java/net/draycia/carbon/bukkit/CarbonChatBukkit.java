package net.draycia.carbon.bukkit;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.nio.file.Path;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.command.BukkitCommander;
import net.draycia.carbon.bukkit.command.BukkitPlayerCommander;
import net.draycia.carbon.common.CarbonChatCommon;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessageService;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class CarbonChatBukkit extends CarbonChatCommon {

    private final UserManager userManager;
    private final Logger logger;
    private final CarbonChatBukkitEntry plugin;
    private final CarbonServer carbonServerBukkit;
    private final CarbonMessageService messageService;
    private final ChannelRegistry channelRegistry;

    @Inject
    private CarbonChatBukkit(
        final CarbonChatBukkitEntry plugin,
        final Logger logger,
        final CarbonServer carbonServerBukkit,
        final UserManager userManager,
        final CarbonMessageService messageService,
        final ChannelRegistry channelRegistry
    ) {
        this.userManager = userManager;
        this.logger = logger;
        this.plugin = plugin;
        this.carbonServerBukkit = carbonServerBukkit;
        this.messageService = messageService;
        this.channelRegistry = channelRegistry;

        CarbonChatProvider.register(this);
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
        return this.plugin.getDataFolder().toPath();
    }

    @Override
    public CarbonServer server() {
        return this.carbonServerBukkit;
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
        final PaperCommandManager<Commander> commandManager;
        try {
            commandManager = new PaperCommandManager<>(
                this.plugin,
                AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
                commandSender -> {
                    if (commandSender instanceof Player player) {
                        return new BukkitPlayerCommander(this, player);
                    }
                    return BukkitCommander.from(commandSender);
                },
                commander -> ((BukkitCommander) commander).commandSender()
            );
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to initialize command manager.", ex);
        }
        commandManager.registerAsynchronousCompletions();
        commandManager.registerBrigadier();
        final var brigadierManager = commandManager.brigadierManager();
        if (brigadierManager != null) {
            brigadierManager.setNativeNumberSuggestions(false);
        }
        return commandManager;
    }

}

package net.draycia.carbon.bukkit;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.command.BukkitCommander;
import net.draycia.carbon.bukkit.command.BukkitPlayerCommander;
import net.draycia.carbon.bukkit.listeners.BukkitChatListener;
import net.draycia.carbon.bukkit.users.MemoryUserManagerBukkit;
import net.draycia.carbon.common.CarbonChatCommon;
import net.draycia.carbon.common.command.Commander;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.nio.file.Path;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonChatBukkit extends CarbonChatCommon {

    private UserManager userManager;
    private Logger logger = LogManager.getLogger("CarbonChat");
    private CarbonChatBukkitEntry plugin;
    private CarbonServerBukkit carbonServerBukkit;
    private Injector injector;

    CarbonChatBukkit(final CarbonChatBukkitEntry plugin) {
        this.plugin = plugin;
        this.injector = Guice.createInjector(this);

        // configure at some point ?

        Bukkit.getPluginManager().registerEvents(
            this.injector.getInstance(BukkitChatListener.class), this.plugin);

        super.initialize();
    }

    @Override
    public void configure() {
        this.bind(CarbonChat.class).toInstance(this);
        this.bind(Logger.class).toInstance(logger);
        this.bind(CarbonChatBukkitEntry.class).toInstance(plugin);

        this.bind(CarbonServer.class).to(CarbonServerBukkit.class);
        this.carbonServerBukkit = this.injector.getInstance(CarbonServerBukkit.class);

        this.bind(UserManager.class).to(MemoryUserManagerBukkit.class);
        this.userManager = this.injector.getInstance(MemoryUserManagerBukkit.class);
    }

    public Injector injector() {
        return this.injector;
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

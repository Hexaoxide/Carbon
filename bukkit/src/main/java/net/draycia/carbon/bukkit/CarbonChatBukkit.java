package net.draycia.carbon.bukkit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.papermc.lib.PaperLib;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Level;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.listeners.BukkitChatListener;
import net.draycia.carbon.bukkit.listeners.BukkitPlayerJoinListener;
import net.draycia.carbon.bukkit.users.CarbonPlayerBukkit;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.listeners.DeafenHandler;
import net.draycia.carbon.common.listeners.MuteHandler;
import net.draycia.carbon.common.messages.CarbonMessageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonChatBukkit extends JavaPlugin implements CarbonChat {

    private static final Set<Class<? extends Listener>> LISTENER_CLASSES = Set.of(
        BukkitChatListener.class,
        BukkitPlayerJoinListener.class
    );
    private static final int BSTATS_PLUGIN_ID = 8720;
    private final CarbonEventHandler eventHandler = new CarbonEventHandler();
    private @MonotonicNonNull Injector injector;
    private @MonotonicNonNull UserManager userManager;
    private @MonotonicNonNull Logger logger;
    private @MonotonicNonNull CarbonServerBukkit carbonServerBukkit;
    private @MonotonicNonNull CarbonMessageService messageService;
    private @MonotonicNonNull ChannelRegistry channelRegistry;

    @Override
    public void onLoad() {
        if (!PaperLib.isPaper()) {
            this.getLogger().log(Level.SEVERE, "*");
            this.getLogger().log(Level.SEVERE, "* CarbonChat makes extensive use of APIs added by Paper.");
            this.getLogger().log(Level.SEVERE, "* For this reason, CarbonChat is not compatible with Spigot or CraftBukkit servers.");
            this.getLogger().log(Level.SEVERE, "* Upgrade your server to Paper in order to use CarbonChat.");
            this.getLogger().log(Level.SEVERE, "*");
            PaperLib.suggestPaper(this, Level.SEVERE);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        CarbonChatProvider.register(this);

        this.injector = Guice.createInjector(new CarbonChatBukkitModule(
            this, this.getDataFolder().toPath()));

        this.logger = LogManager.getLogger("CarbonChat");
        this.messageService = this.injector.getInstance(CarbonMessageService.class);
        this.channelRegistry = this.injector.getInstance(ChannelRegistry.class);
        this.carbonServerBukkit = this.injector.getInstance(CarbonServerBukkit.class);
        this.userManager = this.injector.getInstance(UserManager.class);
    }

    @Override
    public void onEnable() {
        final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

        for (final Class<? extends Listener> listenerClass : LISTENER_CLASSES) {
            this.getServer().getPluginManager().registerEvents(
                this.injector.getInstance(listenerClass),
                this
            );
        }

        // TODO: abstract?
        this.injector.getInstance(MuteHandler.class);
        this.injector.getInstance(DeafenHandler.class);

        final long saveDelay = 5 * 60 * 20;

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this,
            this::savePlayers, saveDelay, saveDelay);

        ((CarbonChannelRegistry) this.channelRegistry()).loadChannels();
    }

    @Override
    public void onDisable() {
        this.savePlayers();
    }

    private void savePlayers() {
        for (final var player : this.server().players()) {
            this.userManager().savePlayer(((CarbonPlayerBukkit) player).carbonPlayer()).thenAccept(result -> {
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
        return this.getDataFolder().toPath();
    }

    @Override
    public CarbonServerBukkit server() {
        return this.carbonServerBukkit;
    }

    @Override
    public ChannelRegistry channelRegistry() {
        return this.channelRegistry;
    }

    public CarbonMessageService messageService() {
        return this.messageService;
    }

    @Override
    public final @NonNull CarbonEventHandler eventHandler() {
        return this.eventHandler;
    }

}

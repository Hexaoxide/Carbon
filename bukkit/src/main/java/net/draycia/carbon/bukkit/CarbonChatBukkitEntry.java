package net.draycia.carbon.bukkit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.papermc.lib.PaperLib;
import java.util.Set;
import java.util.logging.Level;
import net.draycia.carbon.bukkit.listeners.BukkitChatListener;
import net.draycia.carbon.bukkit.users.CarbonPlayerBukkit;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

@Singleton
public final class CarbonChatBukkitEntry extends JavaPlugin {

    private static final Set<Class<? extends Listener>> LISTENER_CLASSES = Set.of(
        BukkitChatListener.class
    );
    private static final int BSTATS_PLUGIN_ID = 8720;

    private @MonotonicNonNull CarbonChatBukkit carbon;
    private @MonotonicNonNull Injector injector;

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

        this.injector = Guice.createInjector(new CarbonChatBukkitModule(
            this,
            this.getDataFolder().toPath(),
            this.getFile().toPath()
        ));

        this.carbon = this.injector.getInstance(CarbonChatBukkit.class);
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

        this.carbon.initialize();

        final long saveDelay = 5 * 60 * 20;

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this,
            this::savePlayers, saveDelay, saveDelay);
    }

    @Override
    public void onDisable() {
        this.savePlayers();
    }

    private void savePlayers() {
        for (final var player : this.carbon.server().players()) {
            this.carbon.userManager().savePlayer(((CarbonPlayerBukkit) player).carbonPlayer()).thenAccept(result -> {
                if (result.player() == null) {
                    this.carbon.server().console().sendMessage(result.reason());
                }
            });
        }
    }

}

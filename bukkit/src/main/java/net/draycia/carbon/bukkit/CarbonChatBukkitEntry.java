package net.draycia.carbon.bukkit;

import com.google.inject.Singleton;
import io.papermc.lib.PaperLib;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.logging.Level;

@Singleton
public final class CarbonChatBukkitEntry extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 8720;

    private @MonotonicNonNull CarbonChatBukkit carbon;

    @Override
    public void onEnable() {
        if (!PaperLib.isPaper()) {
            this.getLogger().log(Level.SEVERE, "*");
            this.getLogger().log(Level.SEVERE, "* CarbonChat makes extensive use of APIs added by Paper.");
            this.getLogger().log(Level.SEVERE, "* For this reason, CarbonChat is not compatible with Spigot or CraftBukkit servers.");
            this.getLogger().log(Level.SEVERE, "* Upgrade your server to Paper in order to use CarbonChat.");
            this.getLogger().log(Level.SEVERE, "*");
            PaperLib.suggestPaper(this, Level.SEVERE);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.carbon = new CarbonChatBukkit(this);

        final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
    }

    @Override
    public void onDisable() {
    }

}

package net.draycia.carbon.bukkit;

import io.papermc.lib.PaperLib;
import net.draycia.carbon.api.CarbonChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.logging.Level;

public final class CarbonChatBukkit extends JavaPlugin implements CarbonChat {
  private final Logger logger = LogManager.getLogger("CarbonChat");

  private static final int BSTATS_PLUGIN_ID = 8720;

  @Override
  public void onEnable() {
    if (!PaperLib.isPaper()) {
      this.logger.error("*");
      this.logger.error("* CarbonChat makes extensive use of APIs added by Paper.");
      this.logger.error("* For this reason, CarbonChat is not compatible with Spigot or CraftBukkit servers.");
      this.logger.error("* Upgrade your server to Paper in order to use CarbonChat.");
      this.logger.error("*");
      PaperLib.suggestPaper(this, Level.SEVERE);
      Bukkit.getPluginManager().disablePlugin(this);
    }

    final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
  }

  @Override
  public void onDisable() {
  }

  @Override
  public @NonNull Logger logger() {
    return this.logger;
  }
}

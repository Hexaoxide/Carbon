package net.draycia.carbon.bukkit;

import io.papermc.lib.PaperLib;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.logging.Level;

public class CarbonChatBukkitEntry extends JavaPlugin {

  private static final int BSTATS_PLUGIN_ID = 8720;

  private @MonotonicNonNull CarbonChatBukkit carbon;

  @Override
  public void onEnable() {
    this.carbon = new CarbonChatBukkit(this);

    if (!PaperLib.isPaper()) {
      this.carbon.logger().error("*");
      this.carbon.logger().error("* CarbonChat makes extensive use of APIs added by Paper.");
      this.carbon.logger().error("* For this reason, CarbonChat is not compatible with Spigot or CraftBukkit servers.");
      this.carbon.logger().error("* Upgrade your server to Paper in order to use CarbonChat.");
      this.carbon.logger().error("*");
      PaperLib.suggestPaper(this, Level.SEVERE);
      Bukkit.getPluginManager().disablePlugin(this);
    }

    final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

    this.carbon.initialize();
  }

  @Override
  public void onDisable() {
  }

}

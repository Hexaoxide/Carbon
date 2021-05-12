package net.draycia.carbon.bukkit;

import io.papermc.lib.PaperLib;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.logging.Level;

import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;

public final class CarbonChatBukkit extends JavaPlugin implements CarbonChat {
  private final Logger logger = LogManager.getLogger("CarbonChat");
  private final CarbonEventHandler eventHandler = new CarbonEventHandler();

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

  @Override
  public @NonNull CarbonEventHandler eventHandler() {
    // TODO: move to common
    return this.eventHandler;
  }

  @Override
  public @NonNull Component createItemHoverComponent(final @NonNull UUID uuid) {
    final Player player = Bukkit.getPlayer(uuid); // This is temporary (it's not)

    if (player == null) {
      return empty();
    }

    final ItemStack itemStack;

    final ItemStack mainHand = player.getInventory().getItemInMainHand();

    if (mainHand != null && !mainHand.getType().isAir()) {
      itemStack = mainHand;
    } else {
      final ItemStack offHand = player.getInventory().getItemInMainHand();

      if (offHand != null && !offHand.getType().isAir()) {
        itemStack = offHand;
      } else {
        itemStack = null;
      }
    }

    if (itemStack == null) {
      return empty();
    }

    if (itemStack.getType().isAir()) {
      return empty();
    }

    final Component displayName = requireNonNullElseGet(itemStack.getItemMeta().displayName(), () ->
      translatable(itemStack.getType().getTranslationKey()));

    return createItemHoverComponent(displayName, itemStack);
  }

}

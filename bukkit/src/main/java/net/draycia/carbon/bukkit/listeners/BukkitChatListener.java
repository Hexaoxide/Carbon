package net.draycia.carbon.bukkit.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BukkitChatListener implements Listener {

  @EventHandler
  public void onPlayerchat(final @NonNull AsyncChatEvent event) {
  }

}

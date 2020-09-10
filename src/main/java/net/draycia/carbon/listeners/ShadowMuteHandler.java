package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.events.ChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ShadowMuteHandler implements Listener {

  @NonNull
  private final CarbonChat carbonChat;

  public ShadowMuteHandler(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onComponent(final ChatComponentEvent event) {
    if (event.sender().shadowMuted()) {
      if (!event.sender().equals(event.target())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void on(final ChatFormatEvent event) {
    if (event.target() != null) {
      return;
    }

    if (!event.sender().shadowMuted()) {
      return;
    }

    final String prefix = this.carbonChat.getModConfig().getString("shadow-mute-prefix", "[SM] ");

    event.format(prefix + event.format());
  }

}

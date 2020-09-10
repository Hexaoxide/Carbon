package net.draycia.carbon.listeners;

import net.draycia.carbon.events.PreChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MuteHandler implements Listener {

  @EventHandler
  public void onMute(PreChatFormatEvent event) {
    if (event.getUser().isMuted()) {
      event.setCancelled(true);
    }
  }

}

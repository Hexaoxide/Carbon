package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChatComponentEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PingHandler implements Listener {

  @NonNull private final CarbonChat carbonChat;

  public PingHandler(@NonNull CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPing(ChatComponentEvent event) {
    if (!carbonChat.getConfig().getBoolean("pings.enabled")) {
      return;
    }

    if (event.getTarget() == null) {
      return;
    }

    String targetName = event.getTarget().asOfflinePlayer().getName();
    String prefix = carbonChat.getConfig().getString("pings.prefix", "");
    boolean caseSensitive = carbonChat.getConfig().getBoolean("pings.case-sensitive", false);

    if (targetName == null) {
      return;
    }

    if (caseSensitive) {
      if (!event.getOriginalMessage().contains(prefix + targetName)) {
        return;
      }
    } else {
      if (!event.getOriginalMessage().toLowerCase().contains((prefix + targetName).toLowerCase())) {
        return;
      }
    }

    Key key = Key.of(carbonChat.getConfig().getString("pings.sound"));
    Sound.Source source = Sound.Source.valueOf(carbonChat.getConfig().getString("pings.source"));
    float volume = (float) carbonChat.getConfig().getDouble("pings.volume");
    float pitch = (float) carbonChat.getConfig().getDouble("pings.pitch");

    event.getTarget().playSound(Sound.of(key, source, volume, pitch));
  }
}

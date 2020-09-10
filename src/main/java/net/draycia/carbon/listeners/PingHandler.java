package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.impls.ChatComponentEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PingHandler implements Listener {

  @NonNull
  private final CarbonChat carbonChat;

  public PingHandler(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPing(final ChatComponentEvent event) {
    if (!this.carbonChat.getConfig().getBoolean("pings.enabled")) {
      return;
    }

    if (event.target() == null) {
      return;
    }

    final String targetName = event.target().offlinePlayer().getName();
    final String prefix = this.carbonChat.getConfig().getString("pings.prefix", "");
    final boolean caseSensitive = this.carbonChat.getConfig().getBoolean("pings.case-sensitive", false);

    if (targetName == null) {
      return;
    }

    if (caseSensitive) {
      if (!event.originalMessage().contains(prefix + targetName)) {
        return;
      }
    } else {
      if (!event.originalMessage().toLowerCase().contains((prefix + targetName).toLowerCase())) {
        return;
      }
    }

    final Key key = Key.of(this.carbonChat.getConfig().getString("pings.sound"));
    final Sound.Source source = Sound.Source.valueOf(this.carbonChat.getConfig().getString("pings.source"));
    final float volume = (float) this.carbonChat.getConfig().getDouble("pings.volume");
    final float pitch = (float) this.carbonChat.getConfig().getDouble("pings.pitch");

    event.target().playSound(Sound.of(key, source, volume, pitch));
  }

}

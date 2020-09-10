package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.impls.PrivateMessageEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class WhisperPingHandler implements Listener {

  @NonNull
  private final CarbonChat carbonChat;

  public WhisperPingHandler(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPing(final PrivateMessageEvent event) {
    if (event.sender().uuid().equals(event.target().uuid())) {
      return;
    }

    final String senderName = event.sender().offlinePlayer().getName();

    if (senderName == null || !event.message().contains(senderName)) {
      return;
    }

    if (!this.carbonChat.getConfig().getBoolean("whisper.pings.enabled")) {
      return;
    }

    final Key key = Key.of(this.carbonChat.getConfig().getString("whisper.pings.sound"));
    final Sound.Source source = Sound.Source.valueOf(this.carbonChat.getConfig().getString("whisper.pings.source"));
    final float volume = (float) this.carbonChat.getConfig().getDouble("whisper.pings.volume");
    final float pitch = (float) this.carbonChat.getConfig().getDouble("whisper.pings.pitch");

    event.sender().playSound(Sound.of(key, source, volume, pitch));
  }

}

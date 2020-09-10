package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.impls.PreChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CapsHandler implements Listener {

  @NonNull
  private final CarbonChat carbonChat;

  public CapsHandler(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onMessage(final PreChatFormatEvent event) {
    if (!this.carbonChat.moderationConfig().getBoolean("caps-protection.enabled")) {
      return;
    }

    if (!(event.message().length() >= this.carbonChat.moderationConfig().getInt("caps-protection.minimum-length"))) {
      return;
    }

    int amountOfCaps = 0;

    for (final char letter : event.message().toCharArray()) {
      if (Character.isUpperCase(letter)) {
        amountOfCaps++;
      }
    }

    final double capsPercentage = (amountOfCaps * 100.0) / event.message().length();

    if (!(capsPercentage >= this.carbonChat.moderationConfig().getDouble("caps-protection.percent-caps"))) {
      return;
    }

    if (this.carbonChat.moderationConfig().getBoolean("block-message")) {
      event.setCancelled(true);
    } else {
      event.message(event.message().toLowerCase());
    }
  }

}

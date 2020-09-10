package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PreChatFormatEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CustomPlaceholderHandler implements Listener {

  private final CarbonChat carbonChat;

  public CustomPlaceholderHandler(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPapiPlaceholder(final PreChatFormatEvent event) {
    final ConfigurationSection placeholders = this.carbonChat.getConfig().getConfigurationSection("placeholders");

    for (final String key : placeholders.getKeys(false)) {
      final String value = placeholders.getString(key);

      event.format(event.format().replace("<" + key + ">", value));
    }
  }

}

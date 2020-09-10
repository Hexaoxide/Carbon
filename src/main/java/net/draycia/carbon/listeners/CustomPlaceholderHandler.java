package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PreChatFormatEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CustomPlaceholderHandler implements Listener {

  private final CarbonChat carbonChat;

  public CustomPlaceholderHandler(CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPapiPlaceholder(PreChatFormatEvent event) {
    ConfigurationSection placeholders = carbonChat.getConfig().getConfigurationSection("placeholders");

    for (String key : placeholders.getKeys(false)) {
      String value = placeholders.getString(key);

      event.setFormat(event.getFormat().replace("<" + key + ">", value));
    }
  }

}

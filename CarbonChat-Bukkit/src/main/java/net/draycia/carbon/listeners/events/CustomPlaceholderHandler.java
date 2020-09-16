package net.draycia.carbon.listeners.events;

import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.kyori.event.PostOrders;
import org.bukkit.configuration.ConfigurationSection;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CustomPlaceholderHandler {

  public CustomPlaceholderHandler(@NonNull final CarbonChatBukkit carbonChat) {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      final ConfigurationSection placeholders = carbonChat.getConfig().getConfigurationSection("placeholders");

      for (final String key : placeholders.getKeys(false)) {
        final String value = placeholders.getString(key);

        event.format(event.format().replace("<" + key + ">", value));
      }
    });
  }

}

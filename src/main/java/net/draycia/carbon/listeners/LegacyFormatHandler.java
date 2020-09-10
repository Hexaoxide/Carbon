package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class LegacyFormatHandler implements Listener {

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onLegacyFormat(PreChatFormatEvent event) {
    Component component = CarbonChat.LEGACY.deserialize(event.getFormat());
    event.setFormat(MiniMessage.get().serialize(component));
  }

}

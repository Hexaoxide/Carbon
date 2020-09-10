package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.impls.PreChatFormatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UserFormattingHandler implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onFormat(final PreChatFormatEvent event) {
    if (!event.user().online()) {
      this.suppressFormatting(event);
      return;
    }

    final Player player = event.user().player();

    if (!player.hasPermission("carbonchat.formatting") &&
      !player.hasPermission("carbonchat.channels." + event.channel().key() + ".formatting")) {
      this.suppressFormatting(event);
    } else {
      // Swap the &-style codes for minimessage-compatible strings
      event.message(MiniMessage.get().serialize(CarbonChat.LEGACY.deserialize(event.message())));
    }
  }

  private void suppressFormatting(final PreChatFormatEvent event) {
    event.format(event.format().replace("<message>", "<pre><message></pre>"));
  }

}

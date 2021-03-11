package net.draycia.carbon.bukkit.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.PostOrders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RelationalPlaceholderHandler {

  public RelationalPlaceholderHandler() {
    CarbonEvents.register(ChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      final CarbonUser recipient = event.recipient();

      if (!(recipient instanceof PlayerUser)) {
        return;
      }

      final Player sender = Bukkit.getPlayer(event.sender().uuid());
      final Player target = Bukkit.getPlayer(((PlayerUser) recipient).uuid());

      if (sender == null || target == null) {
        return;
      }

      event.format(PlaceholderAPI.setRelationalPlaceholders(sender, target, event.format()));
    });
  }

}

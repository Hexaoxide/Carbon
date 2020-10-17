package net.draycia.carbon.bukkit.listeners.contexts;

import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ReceiverContextEvent;
import net.draycia.carbon.api.Context;
import net.kyori.event.PostOrders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DistanceContext {

  private @NonNull static final String KEY = "distance";

  public DistanceContext() {
    CarbonEvents.register(ReceiverContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context == null) {
        return;
      }

      if (context.isBoolean() && !context.asBoolean()) {
        return;
      }

      final Player sender = Bukkit.getPlayer(event.sender().uuid());
      final Player recipient = Bukkit.getPlayer(event.recipient().uuid());

      if (sender == null || recipient == null) {
        event.cancelled(true);
        return;
      }

      final Location senderLocation = sender.getLocation();
      final Location targetLocation = recipient.getLocation();

      if (!senderLocation.getWorld().equals(targetLocation.getWorld())) {
        event.cancelled(true);
        return;
      }

      if (context.isNumber()) {
        final double value = context.asNumber().doubleValue();

        if (value > 0) {
          event.cancelled(senderLocation.distance(targetLocation) > value);
        }
      }
    });
  }

}

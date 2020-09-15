package net.draycia.carbon.listeners.contexts;

import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ReceiverContextEvent;
import net.draycia.carbon.api.Context;
import net.kyori.event.PostOrders;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DistanceContext {

  @NonNull
  private static final String KEY = "distance";

  public DistanceContext() {
    CarbonEvents.register(ReceiverContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY);

      if (context == null) {
        return;
      }

      if (context.isBoolean() && !context.asBoolean()) {
        return;
      }

      if (!event.sender().online() || !event.recipient().online()) {
        event.cancelled(true);
        return;
      }

      final Location senderLocation = event.sender().player().getLocation();
      final Location targetLocation = event.recipient().player().getLocation();

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

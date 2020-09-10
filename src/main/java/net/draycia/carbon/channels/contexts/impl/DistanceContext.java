package net.draycia.carbon.channels.contexts.impl;

import net.draycia.carbon.channels.contexts.MessageContext;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

public class DistanceContext implements Function<MessageContext, Boolean> {

  @Override
  @NonNull
  public Boolean apply(@NonNull MessageContext context) {
    if (!context.getSender().isOnline() || !context.getTarget().isOnline()) {
      return false;
    }

    if (context.isBoolean() && !context.asBoolean()) {
      return true;
    }

    Location senderLocation = context.getSender().asPlayer().getLocation();
    Location targetLocation = context.getTarget().asPlayer().getLocation();

    if (!senderLocation.getWorld().equals(targetLocation.getWorld())) {
      return false;
    }

    if (context.isDouble()) {
      Double value = context.asDouble();

      if (value > 0) {
        return senderLocation.distance(targetLocation) <= value;
      }
    }

    return true;
  }

}

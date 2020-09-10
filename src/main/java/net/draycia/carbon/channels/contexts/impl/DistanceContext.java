package net.draycia.carbon.channels.contexts.impl;

import net.draycia.carbon.channels.contexts.MessageContext;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

public class DistanceContext implements Function<MessageContext, Boolean> {

  @Override
  @NonNull
  public Boolean apply(@NonNull final MessageContext context) {
    if (!context.sender().online() || !context.target().online()) {
      return false;
    }

    if (context.isBoolean() && !context.asBoolean()) {
      return true;
    }

    final Location senderLocation = context.sender().player().getLocation();
    final Location targetLocation = context.target().player().getLocation();

    if (!senderLocation.getWorld().equals(targetLocation.getWorld())) {
      return false;
    }

    if (context.isDouble()) {
      final Double value = context.asDouble();

      if (value > 0) {
        return senderLocation.distance(targetLocation) <= value;
      }
    }

    return true;
  }

}

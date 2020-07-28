package net.draycia.simplechat.channels.contexts.impl;

import net.draycia.simplechat.channels.contexts.MessageContext;
import org.bukkit.Location;

import java.util.function.Function;

public class DistanceContext implements Function<MessageContext, Boolean> {

    @Override
    public Boolean apply(MessageContext context) {
        if (!context.getSender().isOnline() || !context.getTarget().isOnline()) {
            return false;
        }

        Location senderLocation = context.getSender().asPlayer().getLocation();
        Location targetLocation = context.getTarget().asPlayer().getLocation();

        if (!senderLocation.getWorld().equals(targetLocation.getWorld())) {
            return false;
        }

        Double value = (Double) context.getValue();

        if (value > 0) {
            return senderLocation.distance(targetLocation) <= value;
        }

        return true;
    }

}

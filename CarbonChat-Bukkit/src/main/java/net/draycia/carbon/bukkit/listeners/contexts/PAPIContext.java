package net.draycia.carbon.bukkit.listeners.contexts;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.api.events.MessageContextEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ReceiverContextEvent;
import net.draycia.carbon.api.Context;
import net.kyori.event.PostOrders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PAPIContext {

  private final static @NonNull String KEY = "placeholder";
  private final static @NonNull String SENDER = "-sender";
  private final static @NonNull String RECEIVER = "-receiver";

  public PAPIContext() {
    CarbonEvents.register(ReceiverContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY + RECEIVER);

      if (context == null) {
        return;
      }

      if (!context.isString()) {
        return;
      }

      final String[] pieces = context.asString().split("=");

      if (pieces.length != 2) {
        return;
      }

      final Player receiver = Bukkit.getPlayer(event.recipient().uuid());

      if (receiver == null) {
        return;
      }

      final String output = PlaceholderAPI.setPlaceholders(receiver, pieces[0]);

      event.cancelled(output.equals(pieces[1]));
    });

    CarbonEvents.register(MessageContextEvent.class, PostOrders.NORMAL, false, event -> {
      final Context context = event.channel().context(KEY + SENDER);

      if (context == null) {
        return;
      }

      if (!context.isString()) {
        return;
      }

      final String[] pieces = context.asString().split("=");

      if (pieces.length != 2) {
        return;
      }

      final Player receiver = Bukkit.getPlayer(event.user().uuid());

      if (receiver == null) {
        return;
      }

      final String output = PlaceholderAPI.setPlaceholders(receiver, pieces[0]);

      event.cancelled(output.equals(pieces[1]));
    });
  }

}

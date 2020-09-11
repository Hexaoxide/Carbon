package net.draycia.carbon.events;

import net.kyori.event.EventSubscriber;
import net.kyori.event.PostResult;
import net.kyori.event.SimpleEventBus;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CarbonEvents {

  private CarbonEvents() {
  }

  private static final SimpleEventBus<CarbonEvent> EVENT_BUS = new SimpleEventBus<>(CarbonEvent.class);

  public static <T extends CarbonEvent> void register(@NonNull final Class<T> clazz, @NonNull final EventSubscriber<T> subscriber) {
    EVENT_BUS.register(clazz, subscriber);
  }

  public static void unregister(@NonNull final EventSubscriber<? super CarbonEvent> subscriber) {
    EVENT_BUS.unregister(subscriber);
  }

  public static PostResult post(@NonNull final CarbonEvent event) {
    return EVENT_BUS.post(event);
  }

}

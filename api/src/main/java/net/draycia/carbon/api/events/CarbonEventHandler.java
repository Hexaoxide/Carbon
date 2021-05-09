package net.draycia.carbon.api.events;

import net.kyori.event.EventSubscriber;
import net.kyori.event.PostResult;
import net.kyori.event.SimpleEventBus;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Consumer;

public final class CarbonEventHandler {

  private final SimpleEventBus<CarbonEvent> eventBus = new SimpleEventBus<>(CarbonEvent.class);

  public <T extends CarbonEvent> void register(final @NonNull Class<T> clazz, final @NonNull EventSubscriber<T> subscriber) {
    this.eventBus.register(clazz, subscriber);
  }

  public <T extends CarbonEvent> void register(final @NonNull Class<T> clazz, final int priority, final boolean consumeCancelled, final @NonNull Consumer<T> consumer) {
    this.eventBus.register(clazz, new EventSubscriber<T>() {
      @Override
      public int postOrder() {
        return priority;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return consumeCancelled;
      }

      @Override
      public void invoke(final @NonNull T event) {
        consumer.accept(event);
      }
    });
  }

  public void unregister(final @NonNull EventSubscriber<? super CarbonEvent> subscriber) {
    this.eventBus.unregister(subscriber);
  }

  public PostResult post(final @NonNull CarbonEvent event) {
    return this.eventBus.post(event);
  }

}

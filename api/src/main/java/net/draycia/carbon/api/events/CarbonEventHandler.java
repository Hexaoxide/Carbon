package net.draycia.carbon.api.events;

import net.kyori.event.EventSubscriber;
import net.kyori.event.PostResult;
import net.kyori.event.SimpleEventBus;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Consumer;

/**
 * Event handler for listening to and emitting carbon events.
 *
 * @since 1.0.0
 */
public final class CarbonEventHandler {

  private final SimpleEventBus<CarbonEvent> eventBus = new SimpleEventBus<>(CarbonEvent.class);

  /**
   * Registers a subscriber for the given event class.
   *
   * @param eventClass the class to listen for
   * @param subscriber the subscriber that's executed when the event is emitted
   * @param <T> the class to listen for
   *
   * @since 1.0.0
   */
  public <T extends CarbonEvent> void register(final @NonNull Class<T> eventClass, final @NonNull EventSubscriber<T> subscriber) {
    this.eventBus.register(eventClass, subscriber);
  }

  /**
   * Registers a subscriber for the given event class.<br>
   * Includes extra values to control when the consumer is executed.
   *
   * @param eventClass the class to listen for
   * @param priority the priority of the consumer
   * @param consumeCancelled if the consumer should be executed if the event is cancelled early
   * @param consumer the consumer that's executed when the event is emitted
   * @param <T> the class to listen for
   *
   * @return the subscriber, so that it may be unregistered
   *
   * @since 2.0.0
   */
  public <T extends CarbonEvent> EventSubscriber<T> register(final @NonNull Class<T> eventClass, final int priority, final boolean consumeCancelled, final @NonNull Consumer<T> consumer) {
    final var subscriber = new EventSubscriber<T>() {
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
    };

    this.eventBus.register(eventClass, subscriber);

    return subscriber;
  }

  /**
   * Unregisters the subscriber and prevents it from being executed.
   *
   * @param subscriber the subscriber
   *
   * @since 1.0.0
   */
  public void unregister(final @NonNull EventSubscriber<? super CarbonEvent> subscriber) {
    this.eventBus.unregister(subscriber);
  }

  /**
   * Emits the supplied event and returns its result.<br>
   * Events are modified in place, so care should be taken to keep a reference to
   *     the event while it's being emitted.
   *
   * @param event the event to be emitted
   *
   * @return the result
   *
   * @since 2.0.0
   */
  public PostResult emit(final @NonNull CarbonEvent event) {
    return this.eventBus.post(event);
  }

}

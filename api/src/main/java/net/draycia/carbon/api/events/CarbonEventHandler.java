package net.draycia.carbon.api.events;

import java.util.function.Consumer;
import net.kyori.event.EventBus;
import net.kyori.event.EventSubscriber;
import net.kyori.event.EventSubscription;
import net.kyori.event.PostResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Event handler for listening to and emitting carbon events.
 *
 * @since 1.0.0
 */
@DefaultQualifier(NonNull.class)
public final class CarbonEventHandler {

    private final EventBus<CarbonEvent> eventBus = EventBus.create(CarbonEvent.class, (type, event, subscriber) -> {
        if (event instanceof ResultedCarbonEvent<?> rce) {
            return !rce.result().cancelled();
        }
        return true;
    });

    /**
     * Registers a subscriber for the given event class.
     *
     * @param eventClass the class to listen for
     * @param subscriber the subscriber that's executed when the event is emitted
     * @param <T>        the class to listen for
     * @return           the subscription, so that it may be unregistered
     * @since 2.0.0
     */
    public <T extends CarbonEvent> EventSubscription subscribe(
        final Class<T> eventClass,
        final EventSubscriber<T> subscriber
    ) {
        return this.eventBus.subscribe(eventClass, subscriber);
    }

    /**
     * Registers a subscriber for the given event class.<br>
     * Includes extra values to control when the consumer is executed.
     *
     * @param eventClass       the class to listen for
     * @param priority         the priority of the consumer
     * @param acceptsCancelled if the consumer should be executed if the event is cancelled early
     * @param consumer         the consumer that's executed when the event is emitted
     * @param <T>              the class to listen for
     * @return                 the subscription, so that it may be unregistered
     * @since 2.0.0
     */
    public <T extends CarbonEvent> EventSubscription subscribe(
        final Class<T> eventClass,
        final int priority,
        final boolean acceptsCancelled,
        final Consumer<T> consumer
    ) {
        return this.eventBus.subscribe(eventClass, new EventSubscriberImpl<>(consumer, priority, acceptsCancelled));
    }

    /**
     * Emits the supplied event and returns its result.<br>
     * Events are modified in place, so care should be taken to keep a reference to
     * the event while it's being emitted.
     *
     * @param event the event to be emitted
     * @return the result
     * @since 2.0.0
     */
    public PostResult emit(final CarbonEvent event) {
        return this.eventBus.post(event);
    }

}

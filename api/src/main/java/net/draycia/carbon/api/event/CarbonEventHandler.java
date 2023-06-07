package net.draycia.carbon.api.event;

import com.seiama.event.EventSubscriber;
import com.seiama.event.EventSubscription;
import java.util.function.Consumer;

public interface CarbonEventHandler {

    /**
     * Registers a subscriber for the given event class.
     *
     * @param eventClass the class to listen for
     * @param subscriber the subscriber that's executed when the event is emitted
     * @param <T>        the class to listen for
     * @return           the subscription, so that it may be unregistered
     * @since 2.0.0
     */
    <T extends CarbonEvent> EventSubscription<T> subscribe(
        final Class<T> eventClass,
        final EventSubscriber<T> subscriber
    );

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
    <T extends CarbonEvent> EventSubscription<T> subscribe(
        final Class<T> eventClass,
        final int priority,
        final boolean acceptsCancelled,
        final Consumer<T> consumer
    );

    /**
     * Emits the supplied event.<br>
     * Events are modified in place, so care should be taken to keep a reference to
     * the event while it's being emitted.
     *
     * @param event the event to be emitted
     * @since 2.0.0
     */
    void emit(final CarbonEvent event);

}

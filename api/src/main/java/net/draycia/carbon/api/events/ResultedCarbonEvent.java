package net.draycia.carbon.api.events;

import net.kyori.adventure.text.Component;

/**
 * Represents a {@link CarbonEvent} that has a result.
 *
 * @since 2.0.0
 */
public interface ResultedCarbonEvent<R extends ResultedCarbonEvent.Result> extends CarbonEvent {

    /**
     * The result of the event.
     *
     * @return the event's result
     * @since 2.0.0
     */
    R result();

    /**
     * Sets the result of the event.
     *
     * @param result the result
     * @since 2.0.0
     */
    void result(final R result);

    /**
     * Represents the result of the event.
     *
     * @since 2.0.0
     */
    interface Result {

        /**
         * If the event is cancelled to continue execution.<br>
         *
         * @return if listeners will continue executing
         * @since 2.0.0
         */
        boolean cancelled();

        /**
         * The reason of the result.<br>
         * Typically only present if allowed is false.
         *
         * @return the reason of the result
         * @since 2.0.0
         */
        Component reason();

    }

}

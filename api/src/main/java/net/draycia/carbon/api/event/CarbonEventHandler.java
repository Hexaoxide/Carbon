/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.api.event;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * The {@link CarbonEventHandler} is responsible for managing {@link CarbonEventSubscription event subscriptions}
 * and emitting {@link CarbonEvent events}.
 *
 * @since 3.0.0
 */
@DefaultQualifier(NonNull.class)
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
    <T extends CarbonEvent> CarbonEventSubscription<T> subscribe(
        Class<T> eventClass,
        CarbonEventSubscriber<T> subscriber
    );

    /**
     * Registers a subscriber for the given event class.<br>
     * Includes extra values to control when the consumer is executed.
     *
     * @param eventClass       the class to listen for
     * @param order            the order of the consumer
     * @param acceptsCancelled if the consumer should be executed if the event is cancelled early
     * @param subscriber       the consumer that's executed when the event is emitted
     * @param <T>              the class to listen for
     * @return                 the subscription, so that it may be unregistered
     * @since 2.0.0
     */
    <T extends CarbonEvent> CarbonEventSubscription<T> subscribe(
        Class<T> eventClass,
        int order,
        boolean acceptsCancelled,
        CarbonEventSubscriber<T> subscriber
    );

    /**
     * Emits the supplied event.
     *
     * <p>Events are modified in place, meaning you must keep a reference to the event
     * yourself if you wish to inspect it's state after this call.</p>
     *
     * @param event the event to be emitted
     * @param <T> the class to emit
     * @since 2.0.0
     */
    <T extends CarbonEvent> void emit(T event);

}

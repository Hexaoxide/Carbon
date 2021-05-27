/*
 * This file is part of event, licensed under the MIT License.
 *
 * Copyright (c) 2017-2021 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.draycia.carbon.api.events.bus;

import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An event bus.
 *
 * @param <E> the event type
 * @since 5.0.0
 */
public interface EventBus<E> {

    /**
     * Creates an event bus.
     *
     * @param type the event type
     * @param <E>  the event type
     * @return an event bus
     * @since 5.0.0
     */
    static <E> @NonNull EventBus<E> create(final @NonNull Class<E> type) {
        return new EventBusImpl<>(type);
    }

    /**
     * Gets the type of events accepted by this event bus.
     *
     * <p>This is represented by the <code>E</code> type parameter.</p>
     *
     * @return the event type
     * @since 5.0.0
     */
    @NonNull Class<E> type();

    /**
     * Posts an event to all registered subscribers.
     *
     * @param event the event
     * @return the post result of the operation
     * @since 2.0.0
     */
    @NonNull PostResult post(final @NonNull E event);

    /**
     * Determines whether or not the specified event has been subscribed to.
     *
     * @param type the event type
     * @return {@code true} if the event has subscribers, {@code false} otherwise
     * @since 5.0.0
     */
    boolean subscribed(final @NonNull Class<? extends E> type);

    /**
     * Registers the given {@code subscriber} to receive events.
     *
     * @param event      the event type
     * @param subscriber the subscriber
     * @param <T>        the event type
     * @return an event subscription
     * @since 5.0.0
     */
    <T extends E> @NonNull EventSubscription subscribe(final @NonNull Class<T> event, final @NonNull EventSubscriber<? super T> subscriber);

    /**
     * Unregisters all subscribers matching the {@code predicate}.
     *
     * @param predicate the predicate to test subscribers for removal
     * @since 5.0.0
     */
    void unsubscribeIf(final @NonNull Predicate<EventSubscriber<? super E>> predicate);

}

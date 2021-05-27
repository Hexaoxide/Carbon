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

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An event subscriber.
 *
 * @param <E> the event type
 * @since 2.0.0
 */
@FunctionalInterface
public interface EventSubscriber<E> {

    /**
     * Invokes this event subscriber.
     *
     * @param event the event
     * @since 5.0.0
     */
    void on(final @NonNull E event) throws Throwable;

    /**
     * Gets the post order this subscriber should be called at.
     *
     * @return the post order of this subscriber
     * @since 2.0.0
     */
    default int postOrder() {
        return PostOrders.NORMAL;
    }

    /**
     * Gets if cancelled events should be consumed by this subscriber.
     *
     * @return {@code true} if cancelled events should be consumed, {@code false} otherwise
     * @since 5.0.0
     */
    default boolean acceptsCancelled() {
        return true;
    }

}

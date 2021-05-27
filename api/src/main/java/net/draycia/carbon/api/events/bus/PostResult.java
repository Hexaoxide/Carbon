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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Encapsulates the outcome of a {@link EventBus#post(Object)} call.
 *
 * @since 2.0.0
 */
public abstract class PostResult {

    PostResult() {
        if (!(this instanceof Success || this instanceof Failure)) {
            throw new IllegalStateException();
        }
    }

    /**
     * Marks that no exceptions were thrown by subscribers.
     *
     * @return a {@link PostResult} indicating success
     * @since 2.0.0
     */
    public static @NonNull PostResult success() {
        return Success.INSTANCE;
    }

    /**
     * Marks that exceptions were thrown by subscribers.
     *
     * @param exceptions the exceptions that were thrown
     * @return a {@link PostResult} indicating failure
     * @since 2.0.0
     */
    public static @NonNull PostResult failure(final @NonNull Map<EventSubscriber<?>, Throwable> exceptions) {
        if (exceptions.isEmpty()) {
            throw new IllegalStateException("no exceptions present");
        }
        return new Failure(new HashMap<>(exceptions));
    }

    /**
     * Gets if the {@link EventBus#post(Object)} call was successful.
     *
     * @return if the call was successful
     * @since 2.0.0
     */
    public abstract boolean wasSuccessful();

    /**
     * Gets the exceptions that were thrown whilst posting the event to subscribers.
     *
     * @return the exceptions thrown by subscribers
     * @since 3.0.0
     */
    public abstract @NonNull Map<EventSubscriber<?>, Throwable> exceptions();

    /**
     * Raises a {@link CompositeException} if the posting was not
     * {@link #wasSuccessful() successful}.
     *
     * @throws CompositeException if posting was not successful
     * @since 3.0.0
     */
    public abstract void raise() throws CompositeException;

    @Override
    public abstract String toString();

    private static final class Success extends PostResult {
        static final Success INSTANCE = new Success();

        @Override
        public boolean wasSuccessful() {
            return true;
        }

        @Override
        public @NonNull Map<EventSubscriber<?>, Throwable> exceptions() {
            return Collections.emptyMap();
        }

        @Override
        public void raise() {
        }

        @Override
        public String toString() {
            return "PostResult.success()";
        }
    }

    private static final class Failure extends PostResult {
        private final Map<EventSubscriber<?>, Throwable> exceptions;

        private Failure(final @NonNull Map<EventSubscriber<?>, Throwable> exceptions) {
            this.exceptions = exceptions;
        }

        @Override
        public boolean wasSuccessful() {
            return this.exceptions.isEmpty();
        }

        @Override
        public @NonNull Map<EventSubscriber<?>, Throwable> exceptions() {
            return this.exceptions;
        }

        @Override
        public void raise() throws CompositeException {
            throw new CompositeException(this);
        }

        @Override
        public String toString() {
            return "PostResult.failure(" + this.exceptions + ")";
        }
    }

    /**
     * Exception encapsulating a combined {@link #failure(Map) failure}.
     *
     * @since 3.0.0
     */
    @SuppressWarnings("serial")
    public static final class CompositeException extends Exception {
        private final PostResult result;

        CompositeException(final @NonNull PostResult result) {
            super("Exceptions occurred whilst posting to subscribers");
            this.result = result;
        }

        /**
         * Gets the result that created this composite exception.
         *
         * @return the result
         * @since 5.0.0
         */
        public @NonNull PostResult result() {
            return this.result;
        }

        /**
         * Prints all of the stack traces involved in the composite exception.
         *
         * @see Exception#printStackTrace()
         * @since 5.0.0
         */
        public void printAllStackTraces() {
            this.printStackTrace();
            for (final Throwable exception : this.result.exceptions().values()) {
                exception.printStackTrace();
            }
        }
    }

}

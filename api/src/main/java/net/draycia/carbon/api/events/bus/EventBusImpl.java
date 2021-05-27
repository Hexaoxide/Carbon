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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.draycia.carbon.api.events.ResultedCarbonEvent;
import net.kyori.coffee.reflection.Types;
import org.checkerframework.checker.nullness.qual.NonNull;

final class EventBusImpl<E> implements EventBus<E> {

    private static final Comparator<EventSubscriber<?>> COMPARATOR = Comparator.comparingInt(EventSubscriber::postOrder);
    private final Map<Class<? extends E>, Collection<? extends Class<?>>> classes = new HashMap<>();
    private final Map<Class<? extends E>, List<EventSubscriber<? super E>>> unbaked = new HashMap<>();
    private final Map<Class<? extends E>, List<EventSubscriber<? super E>>> baked = new HashMap<>();
    private final Object lock = new Object();
    private final Class<E> type;

    EventBusImpl(final Class<E> type) {
        this.type = type;
    }

    @Override
    public @NonNull Class<E> type() {
        return this.type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull PostResult post(@NonNull final E event) {
        Map<EventSubscriber<?>, Throwable> exceptions = null; // save on an allocation
        final List<EventSubscriber<? super E>> subscribers = this.subscribers((Class<? extends E>) event.getClass());
        for (final EventSubscriber<? super E> subscriber : subscribers) {
            if (this.accepts(event, subscriber)) {
                try {
                    subscriber.on(event);
                } catch (final Throwable t) {
                    if (exceptions == null) {
                        exceptions = new HashMap<>();
                    }
                    exceptions.put(subscriber, t);
                }
            }
        }
        if (exceptions == null) {
            return PostResult.success();
        } else {
            return PostResult.failure(exceptions);
        }
    }

    private boolean accepts(final E event, final EventSubscriber<? super E> subscriber) {
        if (event instanceof ResultedCarbonEvent<?> resultedCarbonEvent) {
            return !resultedCarbonEvent.result().cancelled();
        }

        return true;
    }

    @Override
    public boolean subscribed(final @NonNull Class<? extends E> type) {
        return !this.subscribers(type).isEmpty();
    }

    @Override
    public <T extends E> @NonNull EventSubscription subscribe(final @NonNull Class<T> event, final @NonNull EventSubscriber<? super T> subscriber) {
        synchronized (this.lock) {
            final List<EventSubscriber<? super T>> subscribers = yayGenerics(this.unbaked.computeIfAbsent(event, key -> new ArrayList<>()));
            subscribers.add(subscriber);
            this.baked.clear();
        }
        return () -> {
            synchronized (this.lock) {
                final List<EventSubscriber<? super T>> subscribers = yayGenerics(this.unbaked.get(event));
                if (subscribers != null) {
                    subscribers.remove(subscriber);
                    this.baked.clear();
                }
            }
        };
    }

    @Override
    public void unsubscribeIf(final @NonNull Predicate<EventSubscriber<? super E>> predicate) {
        synchronized (this.lock) {
            boolean dirty = false;
            for (final List<EventSubscriber<? super E>> subscribers : this.unbaked.values()) {
                dirty |= subscribers.removeIf(predicate);
            }
            if (dirty) {
                this.baked.clear();
            }
        }
    }

    private List<EventSubscriber<? super E>> subscribers(final @NonNull Class<? extends E> event) {
        synchronized (this.lock) {
            return this.baked.computeIfAbsent(event, this::subscribers0);
        }
    }

    private List<EventSubscriber<? super E>> subscribers0(final @NonNull Class<? extends E> event) {
        final List<EventSubscriber<? super E>> subscribers = new ArrayList<>();
        final Collection<? extends Class<?>> types = this.classes.computeIfAbsent(event, this::findClasses);
        for (final Class<?> type : types) {
            subscribers.addAll(this.unbaked.getOrDefault(type, Collections.emptyList()));
        }
        subscribers.sort(COMPARATOR);
        return subscribers;
    }

    private Collection<? extends Class<?>> findClasses(final Class<?> type) {
        final Collection<? extends Class<?>> classes = Types.ancestors(type);
        classes.removeIf(klass -> !this.type.isAssignableFrom(klass));
        return classes;
    }

    @SuppressWarnings("unchecked")
    private static <T extends U, U> List<U> yayGenerics(final List<T> list) {
        return (List<U>) list;
    }

}

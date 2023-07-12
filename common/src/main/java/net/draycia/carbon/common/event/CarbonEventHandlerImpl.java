/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.event;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.seiama.event.EventConfig;
import com.seiama.event.EventSubscription;
import com.seiama.event.bus.EventBus;
import com.seiama.event.bus.SimpleEventBus;
import com.seiama.event.registry.EventRegistry;
import com.seiama.event.registry.SimpleEventRegistry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.CarbonEventSubscriber;
import net.draycia.carbon.api.event.CarbonEventSubscription;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Event handler for listening to and emitting carbon events.
 *
 * @since 1.0.0
 */
@DefaultQualifier(NonNull.class)
@Singleton
public final class CarbonEventHandlerImpl implements CarbonEventHandler {

    private final Logger logger;

    @Inject
    private CarbonEventHandlerImpl(final Logger logger) {
        this.logger = logger;
    }

    private final EventRegistry<CarbonEvent> eventRegistry = new SimpleEventRegistry<>(CarbonEvent.class);
    private final EventBus<CarbonEvent> eventBus = new SimpleEventBus<>(this.eventRegistry, this::onException);
    private final ThreadLocal<Deque<List<ListenerException<?>>>> threadLocalExceptionStack = ThreadLocal.withInitial(ArrayDeque::new);

    @SuppressWarnings("unchecked")
    private <E> void onException(final EventSubscription<E> subscription, final E event, final Throwable throwable) {
        this.logger.warn("Exception handling event subscription {} for event {}", subscription, event, throwable);
        this.threadLocalExceptionStack.get().peek().add(new ListenerException<>((EventSubscription<Object>) subscription, event, throwable));
    }

    @Override
    public <T extends CarbonEvent> CarbonEventSubscription<T> subscribe(
        final Class<T> eventClass,
        final CarbonEventSubscriber<T> subscriber
    ) {
        this.eventRegistry.subscribe(eventClass, subscriber::on);
        return new CarbonEventSubscriptionImpl<>(eventClass, subscriber);
    }

    // TODO: support EventConfig#exact()
    @Override
    public <T extends CarbonEvent> CarbonEventSubscription<T> subscribe(
        final Class<T> eventClass,
        final int order,
        final boolean acceptsCancelled,
        final CarbonEventSubscriber<T> subscriber
    ) {
        final EventConfig eventConfig = EventConfig.defaults().order(order).acceptsCancelled(acceptsCancelled);
        this.eventRegistry.subscribe(eventClass, eventConfig, subscriber::on);
        return new CarbonEventSubscriptionImpl<>(eventClass, subscriber);
    }

    @Override
    public <T extends CarbonEvent> void emit(final T event) {
        this.threadLocalExceptionStack.get().push(new ArrayList<>());
        this.eventBus.post(event);
        this.threadLocalExceptionStack.get().pop();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends CarbonEvent> EmitResult<T> emitWithResult(final T event) {
        this.threadLocalExceptionStack.get().push(new ArrayList<>());
        this.eventBus.post(event);
        final List<ListenerException<T>> exceptions = (List) this.threadLocalExceptionStack.get().pop();
        if (exceptions.isEmpty()) {
            return (EmitResult<T>) EmitResult.SUCCESS;
        } else {
            return new EmitResult<>(exceptions);
        }
    }

    public record EmitResult<E>(@Nullable List<ListenerException<E>> exceptions) {
        private static final EmitResult<?> SUCCESS = new EmitResult<>(null);

        public boolean noErrors() {
            return this.exceptions == null;
        }
    }

    public record ListenerException<E>(EventSubscription<E> subscription, E event, Throwable throwable) {}

}

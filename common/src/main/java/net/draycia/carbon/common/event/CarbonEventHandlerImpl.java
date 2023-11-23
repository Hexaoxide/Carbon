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
import com.seiama.event.EventSubscriber;
import com.seiama.event.EventSubscription;
import com.seiama.event.bus.EventBus;
import com.seiama.event.bus.SimpleEventBus;
import com.seiama.event.registry.EventRegistry;
import com.seiama.event.registry.SimpleEventRegistry;
import net.draycia.carbon.api.event.Cancellable;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.CarbonEventSubscriber;
import net.draycia.carbon.api.event.CarbonEventSubscription;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
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

    private <E> void onException(final EventBus<? super E> eventBus, final EventSubscription<? super E> subscription, final E event, final Throwable throwable) {
        final Object subscriber = subscription.subscriber() instanceof SubscriberWrapper<?> wrapped
            ? wrapped.carbon
            : subscription.subscriber();
        this.logger.warn("Exception posting event '{}' to subscriber '{}'", event, subscriber, throwable);
    }

    @Override
    public <T extends CarbonEvent> CarbonEventSubscription<T> subscribe(
        final Class<T> eventClass,
        final CarbonEventSubscriber<T> subscriber
    ) {
        return new CarbonEventSubscriptionImpl<>(
            eventClass,
            subscriber,
            this.eventRegistry.subscribe(eventClass, new SubscriberWrapper<>(subscriber, true))
        );
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
        return new CarbonEventSubscriptionImpl<>(
            eventClass,
            subscriber,
            this.eventRegistry.subscribe(eventClass, eventConfig, new SubscriberWrapper<>(subscriber, acceptsCancelled))
        );
    }

    @Override
    public <T extends CarbonEvent> void emit(final T event) {
        this.eventBus.post(event);
    }

    private record SubscriberWrapper<T extends CarbonEvent>(
        CarbonEventSubscriber<T> carbon,
        boolean acceptsCancelled
    ) implements EventSubscriber<T> {

        @Override
        public void on(final T event) throws Throwable {
            // Our events implement seiama Cancellable; but API consumers won't be able to do that
            if (!this.acceptsCancelled && event instanceof Cancellable cancellable && cancellable.cancelled()) {
                return;
            }
            this.carbon.on(event);
        }

    }

}

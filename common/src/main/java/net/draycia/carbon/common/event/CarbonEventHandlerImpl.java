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

import com.seiama.event.EventConfig;
import com.seiama.event.EventSubscriber;
import com.seiama.event.EventSubscription;
import com.seiama.event.bus.EventBus;
import com.seiama.event.bus.SimpleEventBus;
import com.seiama.event.registry.EventRegistry;
import com.seiama.event.registry.SimpleEventRegistry;
import java.util.function.Consumer;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.event.CarbonEventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Event handler for listening to and emitting carbon events.
 *
 * @since 1.0.0
 */
@DefaultQualifier(NonNull.class)
public final class CarbonEventHandlerImpl implements CarbonEventHandler {

    private final EventRegistry<CarbonEvent> eventRegistry = new SimpleEventRegistry<>(CarbonEvent.class);
    private final EventBus<CarbonEvent> eventBus = new SimpleEventBus<>(this.eventRegistry, this::onException);

    private <E> void onException(final EventSubscription<E> subscription, final E event, final Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public <T extends CarbonEvent> EventSubscription<T> subscribe(
        final Class<T> eventClass,
        final EventSubscriber<T> subscriber
    ) {
        return this.eventRegistry.subscribe(eventClass, subscriber);
    }

    // TODO: support EventConfig#exact()
    @Override
    public <T extends CarbonEvent> EventSubscription<T> subscribe(
        final Class<T> eventClass,
        final int order,
        final boolean acceptsCancelled,
        final Consumer<T> consumer
    ) {
        final EventConfig eventConfig = new CarbonEventConfig(order, acceptsCancelled, false);
        return this.eventRegistry.subscribe(eventClass, eventConfig, consumer::accept);
    }

    @Override
    public void emit(final CarbonEvent event) {
        this.eventBus.post(event);
    }

    private record CarbonEventConfig(int order, boolean acceptsCancelled, boolean exact) implements EventConfig {

    }

}

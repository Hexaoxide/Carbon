package net.draycia.carbon.api.events;

import java.util.function.Consumer;
import net.kyori.event.EventSubscriber;
import org.checkerframework.checker.nullness.qual.NonNull;

record EventSubscriberImpl<T extends CarbonEvent>(Consumer<T> consumer, int postOrder, boolean acceptsCancelled) implements EventSubscriber<T> {
    @Override
    public void on(final @NonNull T event) {
        this.consumer.accept(event);
    }
}

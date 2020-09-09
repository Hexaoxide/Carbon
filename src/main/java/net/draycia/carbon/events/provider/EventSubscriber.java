package net.draycia.carbon.events.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventSubscriber {

    private Map<Class<?>, List<Consumer<Object>>> listeners = new HashMap<>();

    public <T> void registerListener(Class<T> type, Consumer<Object> function) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(function);
    }

    public void callEvent(Object event) {
        if (listeners.containsKey(event.getClass())) {
            for (Consumer<Object> listener : listeners.get(event.getClass())) {
                listener.accept(event);
            }
        }
    }

}

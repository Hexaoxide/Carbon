package net.draycia.carbon.common.util;

import com.google.inject.Injector;
import java.util.List;
import net.draycia.carbon.common.listeners.DeafenHandler;
import net.draycia.carbon.common.listeners.ItemLinkHandler;
import net.draycia.carbon.common.listeners.MuteHandler;

public class ListenerUtils {

    private ListenerUtils() {

    }

    public static final List<Class<?>> LISTENER_CLASSES = List.of(DeafenHandler.class, ItemLinkHandler.class,
        MuteHandler.class);

    public static void registerCommonListeners(final Injector injector) {
        for (final var listenerClass : LISTENER_CLASSES) {
            injector.getInstance(listenerClass);
        }
    }

}

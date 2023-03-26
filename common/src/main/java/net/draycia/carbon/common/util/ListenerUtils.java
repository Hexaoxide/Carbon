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
package net.draycia.carbon.common.util;

import com.google.inject.Injector;
import java.util.List;
import net.draycia.carbon.common.listeners.DeafenHandler;
import net.draycia.carbon.common.listeners.IgnoreHandler;
import net.draycia.carbon.common.listeners.ItemLinkHandler;
import net.draycia.carbon.common.listeners.MessagingHandler;
import net.draycia.carbon.common.listeners.MuteHandler;
import net.draycia.carbon.common.listeners.PingHandler;

public final class ListenerUtils {

    private ListenerUtils() {

    }

    public static final List<Class<?>> LISTENER_CLASSES = List.of(DeafenHandler.class, IgnoreHandler.class,
        ItemLinkHandler.class, MessagingHandler.class, MuteHandler.class, PingHandler.class);

    public static void registerCommonListeners(final Injector injector) {
        for (final var listenerClass : LISTENER_CLASSES) {
            injector.getInstance(listenerClass);
        }
    }

}

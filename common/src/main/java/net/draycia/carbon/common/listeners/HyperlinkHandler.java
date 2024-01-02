/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.common.util.Strings.URL_REPLACEMENT_CONFIG;

@DefaultQualifier(NonNull.class)
public class HyperlinkHandler implements Listener {

    @Inject
    public HyperlinkHandler(final CarbonEventHandler events) {
        events.subscribe(CarbonChatEvent.class, 0, false, event -> {
            if (event.sender().hasPermission("carbon.chatlinks")) {
                event.message(event.message().replaceText(URL_REPLACEMENT_CONFIG.get()));
            }
        });
    }

}

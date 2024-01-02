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
package net.draycia.carbon.paper.hooks;

import com.google.inject.Inject;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.common.event.events.CarbonEarlyChatEvent;
import net.draycia.carbon.common.listeners.Listener;
import net.draycia.carbon.common.util.ColorUtils;
import net.draycia.carbon.paper.CarbonChatPaper;
import net.draycia.carbon.paper.users.CarbonPlayerPaper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class PAPIChatHook implements Listener {

    @Inject
    public PAPIChatHook(final CarbonEventHandler events) {
        events.subscribe(CarbonEarlyChatEvent.class, 0, false, event -> {
            if (!CarbonChatPaper.papiLoaded()) {
                return;
            }

            if (!event.sender().hasPermission("carbon.chatplaceholders")) {
                return;
            }

            if (!(event.sender() instanceof CarbonPlayerPaper playerPaper)) {
                return;
            }

            final String papiParsed = PlaceholderAPI.setPlaceholders(playerPaper.bukkitPlayer(), event.message());

            event.message(ColorUtils.legacyToMiniMessage(papiParsed));
        });
    }

}

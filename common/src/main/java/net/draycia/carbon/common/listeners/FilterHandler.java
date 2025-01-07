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
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonEarlyChatEvent;
import net.kyori.adventure.key.Key;

public class FilterHandler implements Listener {

    private final ConfigManager configManager;

    @Inject
    public FilterHandler(
        final CarbonEventHandler events,
        final ConfigManager configManager
    ) {
        this.configManager = configManager;

        events.subscribe(CarbonEarlyChatEvent.class, 0, false, event -> {
            event.message(this.configManager.primaryConfig().applyChatFilters(event.message()));
        });

        events.subscribe(CarbonChatEvent.class, 0, false, event -> {
            event.renderers().add(KeyedRenderer.keyedRenderer(Key.key("carbon", "filter"), ($, recipient, message, $$$) -> {
                if (recipient instanceof CarbonPlayer carbonPlayer) {
                    if (carbonPlayer.applyOptionalChatFilters()) {
                        return this.configManager.primaryConfig().applyOptionalChatFilters(message);
                    }
                }

                return message;
            }));
        });
    }

}

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
package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.util.InventorySlot;
import net.kyori.adventure.text.TextReplacementConfig;

public class ItemLinkHandler implements Listener {

    @Inject
    public ItemLinkHandler(final CarbonEventHandler events) {
        events.subscribe(CarbonChatEvent.class, 2, false, event -> {
            if (!event.sender().hasPermission("carbon.itemlink")) {
                return;
            }

            for (final var slot : InventorySlot.SLOTS) {
                for (final var placeholder : slot.placeholders()) {
                    event.message(
                        event.message()
                            .replaceText(TextReplacementConfig.builder()
                                .matchLiteral("<" + placeholder + ">")
                                // .once()
                                .replacement(builder -> {
                                    final var itemComponent = event.sender().createItemHoverComponent(slot);

                                    return itemComponent == null ? builder : itemComponent;
                                })
                                .build())
                    );
                }
            }
        });
    }

}

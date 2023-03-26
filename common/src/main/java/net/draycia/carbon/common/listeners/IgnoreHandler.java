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
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.CarbonPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class IgnoreHandler {

    @Inject
    public IgnoreHandler(final CarbonEventHandler events) {
        events.subscribe(CarbonChatEvent.class, 0, false, event -> {
            event.recipients().removeIf(entry -> entry instanceof CarbonPlayer carbonPlayer &&
                carbonPlayer.ignoring(event.sender()));
        });
    }

}

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
package net.draycia.carbon.common.event.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.Cancellable;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.users.CarbonPlayer;

public class CarbonEarlyChatEvent implements CarbonEvent, Cancellable {

    private final CarbonPlayer sender;
    private String message;
    private final ChatChannel channel;
    private boolean cancelled = false;

    public CarbonEarlyChatEvent(final CarbonPlayer sender, final String message, final ChatChannel channel) {
        this.sender = sender;
        this.message = message;
        this.channel = channel;
    }

    public CarbonPlayer sender() {
        return this.sender;
    }

    public String message() {
        return this.message;
    }

    public void message(final String message) {
        this.message = message;
    }

    public ChatChannel channel() {
        return this.channel;
    }

    @Override
    public boolean cancelled() {
        return this.cancelled;
    }

    @Override
    public void cancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

}

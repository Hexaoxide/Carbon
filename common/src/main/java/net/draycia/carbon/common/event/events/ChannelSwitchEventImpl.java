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
import net.draycia.carbon.api.event.events.ChannelSwitchEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ChannelSwitchEventImpl implements ChannelSwitchEvent {

    private final CarbonPlayer player;
    private ChatChannel chatChannel;
    public ChannelSwitchEventImpl(final CarbonPlayer player, final ChatChannel chatChannel) {
        this.player = player;
        this.chatChannel = chatChannel;
    }
    @Override
    public CarbonPlayer player() {
        return this.player;
    }

    @Override
    public ChatChannel channel() {
        return this.chatChannel;
    }

    @Override
    public void channel(final ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

}

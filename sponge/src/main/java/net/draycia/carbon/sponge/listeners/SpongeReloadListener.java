/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.events.CarbonReloadEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;

public class SpongeReloadListener {

    final CarbonChat carbonChat;
    final ConfigFactory configFactory;
    final CarbonChannelRegistry channelRegistry;

    @Inject
    public SpongeReloadListener(
        final CarbonChat carbonChat,
        final ConfigFactory configFactory,
        final CarbonChannelRegistry channelRegistry
    ) {
        this.carbonChat = carbonChat;
        this.configFactory = configFactory;
        this.channelRegistry = channelRegistry;
    }

    @Listener
    public void onReload(final RefreshGameEvent event) {
        this.carbonChat.eventHandler().emit(new CarbonReloadEvent());
    }

}

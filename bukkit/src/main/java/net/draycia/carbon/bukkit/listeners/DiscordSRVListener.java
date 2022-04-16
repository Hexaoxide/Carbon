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
package net.draycia.carbon.bukkit.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePostProcessEvent;
import net.draycia.carbon.bukkit.util.CarbonChatHook;
import org.apache.logging.log4j.Logger;

@Singleton
public final class DiscordSRVListener {

    private final Logger logger;

    @Inject
    public DiscordSRVListener(final Logger logger) {
        this.logger = logger;

        this.logger.info("DiscordSRV found! Enabling hook.");
        DiscordSRV.api.subscribe(this);
    }

    @Subscribe
    public void onDiscordReady(final DiscordReadyEvent event) {
        DiscordSRV.getPlugin().getPluginHooks().add(new CarbonChatHook());
        this.logger.info("Successfully enabled DiscordSRV hook!");
    }

    @Subscribe
    public void onGameChat(final GameChatMessagePostProcessEvent event) {
        //event.setCancelled(true);
    }

}

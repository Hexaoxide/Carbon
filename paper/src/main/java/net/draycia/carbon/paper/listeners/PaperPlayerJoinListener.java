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
package net.draycia.carbon.paper.listeners;

import com.google.inject.Inject;
import java.util.Optional;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.common.users.ProfileCache;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.paper.users.CarbonPlayerPaper;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class PaperPlayerJoinListener implements Listener {

    private final CarbonChat carbonChat;
    private final Logger logger;
    private final ProfileCache profileCache;

    @Inject
    public PaperPlayerJoinListener(
        final CarbonChat carbonChat,
        final Logger logger,
        final ProfileCache profileCache
    ) {
        this.carbonChat = carbonChat;
        this.logger = logger;
        this.profileCache = profileCache;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event) {
        this.profileCache.cache(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        this.carbonChat.server().userManager().user(event.getPlayer().getUniqueId()).thenAccept(result -> {
            Optional.ofNullable(result.displayName()).ifPresent(displayName -> {
                final Player player = event.getPlayer();
                player.displayName(displayName);
                player.playerListName(displayName);
            });
        }).exceptionally(thr -> {
            this.carbonChat.logger().warn("Exception handling player join", thr);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(final PlayerQuitEvent event) {
        ((UserManagerInternal<CarbonPlayerPaper>) this.carbonChat.server().userManager())
            .loggedOut(event.getPlayer().getUniqueId())
            .exceptionally(thr -> {
                this.logger.warn("Exception saving data for player " + event.getPlayer().getName() + " with uuid " + event.getPlayer().getUniqueId());
                return null;
            });
    }

}

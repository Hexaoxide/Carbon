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
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.util.PlayerUtils;
import net.draycia.carbon.paper.users.CarbonPlayerPaper;
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
    private final UserManager<CarbonPlayerCommon> userManager;

    @Inject
    public PaperPlayerJoinListener(
        final CarbonChat carbonChat,
        final UserManager<CarbonPlayerCommon> userManager
    ) {
        this.carbonChat = carbonChat;
        this.userManager = userManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        this.carbonChat.server().userManager().carbonPlayer(event.getPlayer().getUniqueId()).thenAccept(result -> {
            if (result.player() == null) {
                return;
            }

            Optional.ofNullable(result.player().displayName()).ifPresent(displayName -> {
                final Player player = event.getPlayer();
                player.displayName(displayName);
                player.playerListName(displayName);
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event) {
        this.carbonChat.server().userManager().carbonPlayer(event.getPlayer().getUniqueId()).thenAccept(result -> {
            if (result.player() == null) {
                return;
            }

            PlayerUtils.saveAndInvalidatePlayer(this.carbonChat.server(), this.userManager, (CarbonPlayerPaper) result.player());
        });
    }

}

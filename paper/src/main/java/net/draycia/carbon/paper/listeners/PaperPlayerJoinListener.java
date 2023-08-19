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
import com.google.inject.Provider;
import java.util.List;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.ProfileCache;
import net.draycia.carbon.common.users.UserManagerInternal;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.common.util.PlayerUtils.joinExceptionHandler;
import static net.draycia.carbon.common.util.PlayerUtils.saveExceptionHandler;

@DefaultQualifier(NonNull.class)
public class PaperPlayerJoinListener implements Listener {

    private final ConfigManager configManager;
    private final Logger logger;
    private final ProfileCache profileCache;
    private final UserManagerInternal<?> userManager;
    private final Provider<MessagingManager> messaging;
    private final PacketFactory packetFactory;

    @Inject
    public PaperPlayerJoinListener(
        final ConfigManager configManager,
        final Logger logger,
        final ProfileCache profileCache,
        final UserManagerInternal<?> userManager,
        final Provider<MessagingManager> messaging,
        final PacketFactory packetFactory
    ) {
        this.configManager = configManager;
        this.logger = logger;
        this.profileCache = profileCache;
        this.userManager = userManager;
        this.messaging = messaging;
        this.packetFactory = packetFactory;
    }

    @EventHandler
    public void onLogin(final PlayerLoginEvent event) {
        this.profileCache.cache(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoinEarly(final PlayerJoinEvent event) {
        this.messaging.get().withPacketService(packetService -> {
            packetService.queuePacket(this.packetFactory.addLocalPlayerPacket(event.getPlayer().getUniqueId(), event.getPlayer().getName()));
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event) {
        this.userManager.user(event.getPlayer().getUniqueId()).exceptionally(joinExceptionHandler(this.logger));

        final @Nullable List<String> suggestions = this.configManager.primaryConfig().customChatSuggestions();

        if (suggestions == null || suggestions.isEmpty()) {
            return;
        }

        event.getPlayer().addAdditionalChatCompletions(suggestions);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(final PlayerQuitEvent event) {
        this.userManager.loggedOut(event.getPlayer().getUniqueId())
            .exceptionally(saveExceptionHandler(this.logger, event.getPlayer().getName(), event.getPlayer().getUniqueId()));
    }

}

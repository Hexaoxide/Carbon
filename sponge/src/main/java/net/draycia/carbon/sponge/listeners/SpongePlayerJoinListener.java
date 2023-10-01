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
import com.google.inject.Provider;
import java.util.List;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.ProfileCache;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.util.PlayerUtils;
import net.draycia.carbon.sponge.users.CarbonPlayerSponge;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import static net.draycia.carbon.common.users.PlayerUtils.joinExceptionHandler;
import static net.draycia.carbon.common.users.PlayerUtils.saveExceptionHandler;

@DefaultQualifier(NonNull.class)
public class SpongePlayerJoinListener {


    private final ConfigManager configManager;
    private final Logger logger;
    private final ProfileCache profileCache;
    private final UserManagerInternal<?> userManager;
    private final Provider<MessagingManager> messaging;
    private final PacketFactory packetFactory;

    @Inject
    public SpongePlayerJoinListener(
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

    @Listener
    public void onLogin(final ServerSideConnectionEvent.Login event) { // or Handshake/Auth
        this.profileCache.cache(event.user().uniqueId(), event.user().name());
    }

    @Listener(order = Order.EARLY)
    public void onJoinEarly(final ServerSideConnectionEvent.Join event) {
        this.messaging.get().withPacketService(packetService -> {
            packetService.queuePacket(this.packetFactory.addLocalPlayerPacket(event.player().uniqueId(), event.player().name()));
        });
    }

    @Listener(order = Order.LATE)
    public void onJoin(final ServerSideConnectionEvent.Join event) {
        this.userManager.user(event.player().uniqueId()).exceptionally(joinExceptionHandler(this.logger, event.player().name(), event.player().uniqueId()));

//        final @Nullable List<String> suggestions = this.configManager.primaryConfig().customChatSuggestions();
//
//        if (suggestions == null || suggestions.isEmpty()) {
//            return;
//        }
//
//        event.player().addAdditionalChatCompletions(suggestions);
    }

    @Listener(order = Order.LATE)
    public void onQuit(final ServerSideConnectionEvent.Disconnect event) {
        this.userManager.loggedOut(event.player().uniqueId())
            .exceptionally(saveExceptionHandler(this.logger, event.player().name(), event.player().uniqueId()));
    }

}

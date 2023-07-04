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
package net.draycia.carbon.velocity.listeners;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.Player;
import java.util.concurrent.atomic.AtomicInteger;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.listeners.ChatListenerInternal;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

@DefaultQualifier(NonNull.class)
public final class VelocityChatListener extends ChatListenerInternal {

    private final UserManager<?> userManager;
    private final Logger logger;
    private final AtomicInteger timesWarned = new AtomicInteger(0);
    private final PluginManager pluginManager;
    final ConfigFactory configFactory;

    @Inject
    private VelocityChatListener(
        final CarbonChat carbonChat,
        final UserManager<?> userManager,
        final Logger logger,
        final PluginManager pluginManager,
        final CarbonMessages carbonMessages,
        final ConfigFactory configFactory
    ) {
        super(carbonChat, carbonMessages, configFactory);
        this.userManager = userManager;
        this.logger = logger;
        this.pluginManager = pluginManager;
        this.configFactory = configFactory;
    }

    @Subscribe(order = PostOrder.LATE)
    public void onPlayerChat(final PlayerChatEvent event) {
        if (!event.getResult().isAllowed()) {
            return;
        }

        final Player player = event.getPlayer();
        final boolean signedVersion = player.getIdentifiedKey() != null
            && player.getProtocolVersion().compareTo(ProtocolVersion.MINECRAFT_1_19_1) >= 0;
        if (signedVersion && !this.pluginManager.isLoaded("unsignedvelocity")) {
            if (this.timesWarned.getAndIncrement() < 3) {
                this.logger.warn("""
                    
                    ==================================================
                    We have avoided modifying {}'s chat ,
                    since they use a version higher than 1.19.1,
                    where this function is not supported.
                    
                    If you want to keep this function working,
                    install UnSignedVelocity.
                    ==================================================
                    """, player.getUsername()
                );
            }
            return;
        }

        event.setResult(PlayerChatEvent.ChatResult.denied());

        final CarbonPlayer sender = this.userManager.user(event.getPlayer().getUniqueId()).join();

        final String content = event.getResult().getMessage().orElse(event.getMessage());
        final CarbonChatEvent chatEvent = this.prepareAndEmitChatEvent(sender, content, null);

        if (chatEvent.cancelled()) {
            return;
        }

        for (final Audience recipient : chatEvent.recipients()) {
            Component renderedMessage = chatEvent.message();

            for (final KeyedRenderer renderer : chatEvent.renderers()) {
                renderedMessage = renderer.render(sender, recipient, renderedMessage, chatEvent.message());
            }

            recipient.sendMessage(renderedMessage);
        }
    }

}

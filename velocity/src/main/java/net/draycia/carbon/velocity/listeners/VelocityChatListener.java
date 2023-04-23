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
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.velocity.CarbonChatVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.draycia.carbon.common.util.Strings.URL_REPLACEMENT_CONFIG;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public final class VelocityChatListener {

    private final CarbonChatVelocity carbonChat;
    private final ChannelRegistry registry;
    private final UserManager<?> userManager;
    private final Logger logger;
    private final AtomicInteger timesWarned = new AtomicInteger(0);
    private final PluginManager pluginManager;

    @Inject
    private VelocityChatListener(
        final CarbonChat carbonChat,
        final ChannelRegistry registry,
        final UserManager<?> userManager,
        final Logger logger,
        final PluginManager pluginManager
    ) {
        this.carbonChat = (CarbonChatVelocity) carbonChat;
        this.registry = registry;
        this.userManager = userManager;
        this.logger = logger;
        this.pluginManager = pluginManager;
    }

    @Subscribe
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
        var channel = requireNonNullElse(sender.selectedChannel(), this.registry.defaultValue());
        final var originalMessage = event.getResult().getMessage().orElse(event.getMessage());
        Component eventMessage = text(originalMessage);

        if (sender.hasPermission("carbon.chatlinks")) {
            eventMessage = eventMessage.replaceText(URL_REPLACEMENT_CONFIG.get());
        }

        for (final var chatChannel : this.registry) {
            if (chatChannel.quickPrefix() == null) {
                continue;
            }

            if (originalMessage.startsWith(chatChannel.quickPrefix()) && chatChannel.speechPermitted(sender).permitted()) {
                channel = chatChannel;
                eventMessage = eventMessage.replaceText(TextReplacementConfig.builder()
                    .once()
                    .matchLiteral(channel.quickPrefix())
                    .replacement(text())
                    .build());
                break;
            }
        }

        final var recipients = channel.recipients(sender);

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(sender, eventMessage, recipients, renderers, channel, null);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (!result.wasSuccessful()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                sender.sendMessage(message);
            }

            return;
        }

        for (final var recipient : chatEvent.recipients()) {
            var renderedMessage = chatEvent.message();

            for (final var renderer : chatEvent.renderers()) {
                renderedMessage = renderer.render(sender, recipient, renderedMessage, chatEvent.message());
            }

            recipient.sendMessage(renderedMessage);
        }

        event.setResult(PlayerChatEvent.ChatResult.denied());
    }

}

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
package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import java.util.regex.Pattern;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.common.config.ConfigFactory;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;

@DefaultQualifier(NonNull.class)
public class PingHandler {

    private final Key muteKey = key("carbon", "pings");
    private final KeyedRenderer renderer;

    @Inject
    public PingHandler(final CarbonChat carbonChat, final ConfigFactory configFactory) {
        //TODO: there is an issue with this, it only works if the player is sending the message pings themself
        this.renderer = keyedRenderer(this.muteKey, (sender, recipient, message, originalMessage) -> {
            if (!(recipient instanceof CarbonPlayer recipientPlayer)) {
                return new RenderedMessage(message, MessageType.CHAT);
            }

            final String prefix = configFactory.primaryConfig().pings().prefix();

            return new RenderedMessage(message.replaceText(TextReplacementConfig.builder()
                .match(Pattern.compile(Pattern.quote(prefix + recipientPlayer.username()), Pattern.CASE_INSENSITIVE))
                .replacement(matchedText -> {
                    if (configFactory.primaryConfig().pings().playSound()) {
                        recipient.playSound(configFactory.primaryConfig().pings().sound());
                    }

                    return Component.text(recipientPlayer.username()).color(configFactory.primaryConfig().pings().highlightTextColor());
                })
                .build()), MessageType.CHAT);
        });

        carbonChat.eventHandler().subscribe(CarbonChatEvent.class, 1, false, event -> {
            event.renderers().add(0, this.renderer);
        });
    }

}

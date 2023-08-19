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
package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.regex.Pattern;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.config.ConfigManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;

@DefaultQualifier(NonNull.class)
@Singleton
public class PingHandler implements Listener {

    private final Key pingKey = key("carbon", "pings");
    private final KeyedRenderer renderer;
    private final ConfigManager configManager;

    @Inject
    public PingHandler(final CarbonEventHandler events, final ConfigManager configManager) {
        this.configManager = configManager;
        this.renderer = keyedRenderer(this.pingKey, (sender, recipient, message, originalMessage) -> {
            if (!(recipient instanceof CarbonPlayer recipientPlayer)) {
                return message;
            }

            return this.convertPings(recipientPlayer, message);
        });

        events.subscribe(CarbonChatEvent.class, 1, false, event -> {
            event.renderers().add(0, this.renderer);
        });
    }

    public Component convertPings(final CarbonPlayer recipient, final Component message) {
        final String prefix = this.configManager.primaryConfig().pings().prefix();
        final String plainDisplayName = PlainTextComponentSerializer.plainText().serialize(recipient.displayName());

        return message.replaceText(TextReplacementConfig.builder()
            // \B(@Username|@Displayname)\b
            .match(Pattern.compile(
                String.format(
                    "\\B%1$s(%2$s|%3$s)\\b",
                    Pattern.quote(prefix),
                    Pattern.quote(recipient.username()),
                    Pattern.quote(plainDisplayName)),
                Pattern.CASE_INSENSITIVE))
            .replacement(matchedText -> {
                if (this.configManager.primaryConfig().pings().playSound()) {
                    recipient.playSound(this.configManager.primaryConfig().pings().sound());
                }

                return Component.text(matchedText.content()).color(this.configManager.primaryConfig().pings().highlightTextColor());
            })
            .build());
    }

}

/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;

@DefaultQualifier(NonNull.class)
public class MuteHandler implements Listener {

    private final Key muteKey = key("carbon", "mute");
    private CarbonMessages carbonMessages;

    private final KeyedRenderer renderer =
        keyedRenderer(this.muteKey, (sender, recipient, message, originalMessage) -> {
            // This is an annoying side effect of the RenderedComponent change
            final var prefix = this.carbonMessages.muteSpyPrefix(recipient);

            return prefix.append(message);
        });

    @Inject
    public MuteHandler(final CarbonEventHandler events, final CarbonMessages carbonMessages) {
        this.carbonMessages = carbonMessages;

        events.subscribe(CarbonChatEvent.class, 100, false, event -> {
            if (!event.sender().muted()) {
                return;
            }

            event.renderers().add(this.renderer);

            event.recipients().removeIf(entry -> entry instanceof CarbonPlayer carbonPlayer &&
                !carbonPlayer.spying());
        });
    }

}

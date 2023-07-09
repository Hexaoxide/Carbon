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
package net.draycia.carbon.common.event.events;

import java.util.Objects;
import net.draycia.carbon.api.event.events.CarbonPrivateChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.event.CancellableImpl;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Called whenever a player privately messages another player.
 *
 * @since 2.1.0
 */
@DefaultQualifier(NonNull.class)
public class CarbonPrivateChatEventImpl extends CancellableImpl implements CarbonPrivateChatEvent {

    private final CarbonPlayer sender;
    private final CarbonPlayer recipient;

    private Component message;

    public CarbonPrivateChatEventImpl(final CarbonPlayer sender, final CarbonPlayer recipient, final Component message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    public void message(final Component message) {
        this.message = Objects.requireNonNull(message);
    }

    public Component message() {
        return this.message;
    }

    public CarbonPlayer sender() {
        return this.sender;
    }

    public CarbonPlayer recipient() {
        return this.recipient;
    }

}

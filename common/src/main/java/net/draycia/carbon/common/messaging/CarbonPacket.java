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
package net.draycia.carbon.common.messaging;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import ninja.egg82.messenger.packets.AbstractPacket;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

public abstract class CarbonPacket extends AbstractPacket {

    private final GsonComponentSerializer componentSerializer = GsonComponentSerializer.gson();

    protected CarbonPacket(@NotNull UUID sender) {
        super(sender);
    }

    protected final void writeComponent(final Component component, final ByteBuf buffer) {
        this.writeString(componentSerializer.serialize(component), buffer);
    }

    protected final Component readComponent(final ByteBuf buffer) {
        return componentSerializer.deserialize(this.readString(buffer));
    }

    protected final void writeKey(final Key key, final ByteBuf buffer) {
        this.writeString(key.asString(), buffer);
    }

    protected final Key readKey(final ByteBuf buffer) {
        final @Subst("carbon:channel") String value = this.readString(buffer);

        return Key.key(value);
    }

}

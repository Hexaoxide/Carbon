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
package net.draycia.carbon.common.messaging.packets;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import ninja.egg82.messenger.packets.AbstractPacket;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

public abstract class CarbonPacket extends AbstractPacket {

    private final GsonComponentSerializer componentSerializer = GsonComponentSerializer.gson();

    protected CarbonPacket(final @NotNull UUID sender) {
        super(sender);
    }

    protected final void writeComponent(final Component component, final ByteBuf buffer) {
        this.writeString(this.componentSerializer.serialize(component), buffer);
    }

    protected final Component readComponent(final ByteBuf buffer) {
        return this.componentSerializer.deserialize(this.readString(buffer));
    }

    protected final void writeKey(final Key key, final ByteBuf buffer) {
        this.writeString(key.asString(), buffer);
    }

    protected final Key readKey(final ByteBuf buffer) {
        final @Subst("carbon:channel") String value = this.readString(buffer);

        return Key.key(value);
    }

    protected final void writeStringMap(final Map<String, String> map, final ByteBuf buffer) {
        this.writeVarInt(map.size(), buffer);

        for (final Map.Entry<String, String> entry : map.entrySet()) {
            this.writeString(entry.getKey(), buffer);
            this.writeString(entry.getValue(), buffer);
        }
    }

    protected final Map<String, String> readStringMap(final ByteBuf buffer) {
        final int size = this.readVarInt(buffer);
        final Map<String, String> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            map.put(this.readString(buffer), this.readString(buffer));
        }

        return map;
    }

}

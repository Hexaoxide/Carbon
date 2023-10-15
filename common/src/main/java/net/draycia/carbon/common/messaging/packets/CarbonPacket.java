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
import java.util.function.BiConsumer;
import java.util.function.Function;
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

    protected final <K, V> void writeMap(
        final Map<K, V> map,
        final BiConsumer<K, ByteBuf> keyWriter,
        final BiConsumer<V, ByteBuf> valueWriter,
        final ByteBuf buffer
    ) {
        this.writeVarInt(map.size(), buffer);

        for (final Map.Entry<K, V> entry : map.entrySet()) {
            keyWriter.accept(entry.getKey(), buffer);
            valueWriter.accept(entry.getValue(), buffer);
        }
    }

    protected final <K, V> Map<K, V> readMap(
        final ByteBuf buffer,
        final Function<ByteBuf, K> keyReader,
        final Function<ByteBuf, V> valueReader
    ) {
        final int size = this.readVarInt(buffer);
        final Map<K, V> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            map.put(keyReader.apply(buffer), valueReader.apply(buffer));
        }

        return map;
    }

    protected final <E extends Enum<E>> void writeEnum(final E value, final ByteBuf buf) {
        this.writeVarInt(value.ordinal(), buf);
    }

    protected final <E extends Enum<E>> E readEnum(final ByteBuf buf, final Class<E> cls) {
        return cls.getEnumConstants()[this.readVarInt(buf)];
    }

}

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

import java.util.UUID;
import net.kyori.adventure.key.Key;
import ninja.egg82.messenger.utils.UUIDUtil;
import org.jetbrains.annotations.NotNull;

public final class ChatMessagePacket extends CarbonPacket {

    // TODO: store item link placeholder components
    private UUID userId;
    private Key channelKey;
    private String username;

    public ChatMessagePacket() {
        super(UUIDUtil.EMPTY_UUID);
    }

    public ChatMessagePacket(
        final @NotNull UUID serverId,
        final UUID userId,
        final Key channelKey,
        final String username
    ) {
        super(serverId);
        this.userId = userId;
        this.channelKey = channelKey;
        this.username = username;
    }

    @Override
    public void read(io.netty.buffer.@NotNull ByteBuf buffer) {
        this.userId = this.readUUID(buffer);
        this.channelKey = this.readKey(buffer);
        this.username = this.readString(buffer);
    }

    @Override
    public void write(io.netty.buffer.@NotNull ByteBuf buffer) {
        this.writeUUID(this.userId, buffer);
        this.writeKey(this.channelKey, buffer);
        this.writeString(this.username, buffer);
    }

}

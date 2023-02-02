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
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import ninja.egg82.messenger.utils.UUIDUtil;
import org.jetbrains.annotations.NotNull;

public final class ChatMessagePacket extends CarbonPacket {

    // TODO: store item link placeholder components
    private UUID userId;
    private String channelPermission;
    private Key channelKey;
    private String username;
    private String intermediary;
    private Map<String, String> placeholders;

    public UUID userId() {
        return this.userId;
    }

    public String channelPermission() {
        return this.channelPermission;
    }

    public Key channelKey() {
        return this.channelKey;
    }

    public String username() {
        return this.username;
    }

    public String intermediary() {
        return this.intermediary;
    }

    public Map<String, String> placeholders() {
        return this.placeholders;
    }

    public ChatMessagePacket(final @NotNull UUID sender, final @NotNull ByteBuf data) {
        super(sender);
        this.read(data);
    }

    public ChatMessagePacket() {
        super(UUIDUtil.EMPTY_UUID);
    }

    public ChatMessagePacket(
        final @NotNull UUID serverId,
        final UUID userId,
        final String channelPermission,
        final Key channelKey,
        final String username,
        final String intermediary,
        final Map<String, String> placeholders
    ) {
        super(serverId);
        this.userId = userId;
        this.channelPermission = channelPermission;
        this.channelKey = channelKey;
        this.username = username;
        this.intermediary = intermediary;
        this.placeholders = placeholders;
    }

    @Override
    public void read(final io.netty.buffer.@NotNull ByteBuf buffer) {
        this.userId = this.readUUID(buffer);
        this.channelPermission = this.readString(buffer);
        this.channelKey = this.readKey(buffer);
        this.username = this.readString(buffer);
        this.intermediary = this.readString(buffer);
        this.placeholders = this.readStringMap(buffer);
    }

    @Override
    public void write(final io.netty.buffer.@NotNull ByteBuf buffer) {
        this.writeUUID(this.userId, buffer);
        this.writeString(this.channelPermission, buffer);
        this.writeKey(this.channelKey, buffer);
        this.writeString(this.username, buffer);
        this.writeString(this.intermediary, buffer);
        this.writeStringMap(this.placeholders, buffer);
    }

}

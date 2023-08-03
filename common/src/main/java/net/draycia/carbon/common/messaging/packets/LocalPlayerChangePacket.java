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

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChat;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class LocalPlayerChangePacket extends CarbonPacket {

    private @MonotonicNonNull UUID playerId;
    private @MonotonicNonNull String playerName;
    private @MonotonicNonNull ChangeType changeType;

    @AssistedInject
    public LocalPlayerChangePacket(
        final CarbonChat carbonChat,
        final @Assisted UUID playerId,
        final @Assisted @Nullable String playerName,
        final @Assisted ChangeType changeType
    ) {
        super(carbonChat.serverId());
        if (changeType == ChangeType.ADD && playerName == null) {
            throw new IllegalArgumentException("playerName cannot be null for ChangeType.ADD");
        }
        this.playerId = playerId;
        this.playerName = playerName;
        this.changeType = changeType;
    }

    public LocalPlayerChangePacket(final UUID sender, final ByteBuf data) {
        super(sender);
        this.read(data);
    }

    public UUID playerId() {
        return this.playerId;
    }

    public String playerName() {
        return this.playerName;
    }

    public ChangeType changeType() {
        return this.changeType;
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.playerId = this.readUUID(buffer);
        final String type = this.readString(buffer);
        this.changeType = ChangeType.valueOf(type);
        if (this.changeType == ChangeType.ADD) {
            this.playerName = this.readString(buffer);
        }
    }

    @Override
    public void write(final ByteBuf buffer) {
        this.writeUUID(this.playerId, buffer);
        this.writeString(this.changeType.name(), buffer);
        if (this.changeType == ChangeType.ADD) {
            this.writeString(this.playerName, buffer);
        }
    }

    public enum ChangeType {
        ADD, REMOVE
    }

}

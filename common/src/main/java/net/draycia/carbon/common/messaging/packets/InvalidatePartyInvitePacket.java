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
package net.draycia.carbon.common.messaging.packets;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.draycia.carbon.common.messaging.ServerId;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class InvalidatePartyInvitePacket extends CarbonPacket {

    private @MonotonicNonNull UUID from;
    private @MonotonicNonNull UUID to;

    @AssistedInject
    public InvalidatePartyInvitePacket(
        final @ServerId UUID serverId,
        final @Assisted("from") UUID from,
        final @Assisted("to") UUID to
    ) {
        super(serverId);
        this.from = from;
        this.to = to;
    }

    public InvalidatePartyInvitePacket(final UUID sender, final ByteBuf data) {
        super(sender);
        this.read(data);
    }

    public UUID from() {
        return this.from;
    }

    public UUID to() {
        return this.to;
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.from = this.readUUID(buffer);
        this.to = this.readUUID(buffer);
    }

    @Override
    public void write(final ByteBuf buffer) {
        this.writeUUID(this.from, buffer);
        this.writeUUID(this.to, buffer);
    }

}

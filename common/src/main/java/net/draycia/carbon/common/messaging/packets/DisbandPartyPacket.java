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
import net.draycia.carbon.common.messaging.ServerId;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class DisbandPartyPacket extends CarbonPacket {

    private @MonotonicNonNull UUID partyId;

    @AssistedInject
    public DisbandPartyPacket(
        final @ServerId UUID serverId,
        final @Assisted UUID partyId
    ) {
        super(serverId);
        this.partyId = partyId;
    }

    public DisbandPartyPacket(final UUID sender, final ByteBuf data) {
        super(sender);
        this.read(data);
    }

    public UUID partyId() {
        return this.partyId;
    }

    @Override
    public void read(final ByteBuf buffer) {
        this.partyId = this.readUUID(buffer);
    }

    @Override
    public void write(final ByteBuf buffer) {
        this.writeUUID(this.partyId, buffer);
    }

    @Override
    public String toString() {
        return "DisbandPartyPacket{" +
            "partyId=" + this.partyId +
            ", sender=" + this.sender +
            '}';
    }

}

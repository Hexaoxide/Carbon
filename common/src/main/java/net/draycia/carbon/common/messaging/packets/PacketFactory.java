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
import java.util.Map;
import java.util.UUID;
import net.draycia.carbon.common.users.PartyImpl;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface PacketFactory {

    SaveCompletedPacket saveCompletedPacket(UUID playerId);

    LocalPlayersPacket localPlayersPacket(Map<UUID, String> players);

    default LocalPlayersPacket clearLocalPlayersPacket() {
        return this.localPlayersPacket(Map.of());
    }

    LocalPlayerChangePacket localPlayerChangePacket(UUID player, @Nullable String name, LocalPlayerChangePacket.ChangeType type);

    default LocalPlayerChangePacket addLocalPlayerPacket(final UUID id, final String name) {
        return this.localPlayerChangePacket(id, name, LocalPlayerChangePacket.ChangeType.ADD);
    }

    default LocalPlayerChangePacket removeLocalPlayerPacket(final UUID id) {
        return this.localPlayerChangePacket(id, null, LocalPlayerChangePacket.ChangeType.REMOVE);
    }

    WhisperPacket whisperPacket(@Assisted("from") UUID from, @Assisted("to") UUID to, Component msg);

    PartyChangePacket partyChange(UUID partyId, Map<UUID, PartyImpl.ChangeType> changes);

    PartyInvitePacket partyInvite(@Assisted("from") UUID from, @Assisted("to") UUID to, @Assisted("party") UUID party);

    PartyInvitePacket invalidatePartyInvite(@Assisted("from") UUID from, @Assisted("to") UUID to);

}

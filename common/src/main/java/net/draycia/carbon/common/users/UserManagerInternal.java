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
package net.draycia.carbon.common.users;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.messaging.packets.DisbandPartyPacket;
import net.draycia.carbon.common.messaging.packets.PartyChangePacket;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface UserManagerInternal<C extends CarbonPlayer> extends UserManager<C> {

    void shutdown();

    CompletableFuture<Void> saveIfNeeded(C player);

    CompletableFuture<Void> loggedOut(UUID uuid);

    void saveCompleteMessageReceived(UUID playerId);

    void cleanup();

    CompletableFuture<Void> saveParty(PartyImpl info);

    void disbandParty(UUID id);

    void partyChangeMessageReceived(PartyChangePacket pkt);

    void disbandPartyMessageReceived(DisbandPartyPacket pkt);
}

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
package net.draycia.carbon.paper.integration.fuuid;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
abstract class AbstractFactionsChannel extends ConfigChatChannel {

    protected final @Nullable Faction faction(final CarbonPlayer player) {
        final @Nullable FPlayer fPlayer = this.factionPlayer(player);

        if (fPlayer == null || !fPlayer.hasFaction()) {
            return null;
        }

        return fPlayer.getFaction();
    }

    protected final @Nullable FPlayer factionPlayer(final CarbonPlayer player) {
        return FPlayers.getInstance().getById(player.uuid().toString());
    }

    protected final @Nullable Role factionRole(final CarbonPlayer player) {
        final @Nullable FPlayer fPlayer = this.factionPlayer(player);

        if (fPlayer == null || !fPlayer.hasFaction()) {
            return null;
        }

        return fPlayer.getRole();
    }

    protected final boolean hasRelations(final CarbonPlayer player, final Relation relation) {
        final @Nullable Faction faction = this.faction(player);

        return faction != null && faction.getRelationCount(relation) > 0;
    }

    @Override
    public boolean shouldCrossServer() {
        return false;
    }

}

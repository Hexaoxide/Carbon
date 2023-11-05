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
package net.draycia.carbon.paper.hooks.towny;

import com.palmergames.bukkit.towny.object.ResidentList;
import java.util.List;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public class TownChannel extends ResidentListChannel {

    private final Key key = Key.key("carbon", "townchat");

    @Override
    public Key key() {
        return this.key;
    }

    @Override
    public List<String> commandAliases() {
        return List.of("tc");
    }

    @Override
    protected @Nullable ResidentList residentList(final CarbonPlayer player) {
        return TOWNY_API.getTown(player.uuid());
    }
}

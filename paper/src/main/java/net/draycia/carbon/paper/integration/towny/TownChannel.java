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
package net.draycia.carbon.paper.integration.towny;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import java.util.Map;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.config.ConfigHeader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
@ConfigHeader(ResidentListChannel.TOWNY_CHANNEL_HEADER)
public class TownChannel extends ResidentListChannel<Town> {

    public static final String FILE_NAME = "towny-townchat.conf";

    public TownChannel() {
        this.key = Key.key("carbon", "townchat");
        this.commandAliases = List.of("tc");

        this.messageSource = new ConfigChannelMessageSource();
        this.messageSource.defaults = Map.of(
            "default_format", "(town: %townyadvanced_town_unformatted%) <display_name>: <message>",
            "console", "[town: %townyadvanced_town_unformatted%] <username>: <message>"
        );
    }

    @Override
    protected @Nullable Town residentList(final CarbonPlayer player) {
        final @Nullable Resident resident = TOWNY_API.getResident(player.uuid());
        if (resident == null) {
            return null;
        }
        return TOWNY_API.getResidentTownOrNull(resident);
    }

    @Override
    protected Component cannotUseChannel(final CarbonPlayer player) {
        return this.messages.cannotUseTownChannel(player);
    }

}

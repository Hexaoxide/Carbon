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

import com.palmergames.bukkit.towny.object.Nation;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public class AllianceChannel extends NationChannel {

    public static final String FILE_NAME = "alliancechat.conf";

    public AllianceChannel() {
        this.key = Key.key("carbon", "alliancechat");
        this.commandAliases = List.of();

        this.messageSource = new ConfigChannelMessageSource();
        this.messageSource.defaults = Map.of(
            "default_format", "(alliance) <display_name>: <message>",
            "console", "[alliance] <username> - <uuid>: <message>"
        );
        this.messageSource.locales = Map.of(
            Locale.US, Map.of("default_format", "(alliance) <display_name>: <message>")
        );
    }

    @Override
    protected List<Player> onlinePlayers(final Nation residentList) {
        return TOWNY_API.getOnlinePlayersAlliance(residentList);
    }

    @Override
    protected void cannotUseChannel(final CarbonPlayer player) {
        this.messages.cannotUseAllianceChannel(player);
    }

}

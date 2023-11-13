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
package net.draycia.carbon.paper.integration.fuuid;

import com.google.inject.Inject;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.draycia.carbon.api.channels.ChannelPermissionResult;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import static net.draycia.carbon.api.channels.ChannelPermissionResult.channelPermissionResult;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public class FactionChannel extends ConfigChatChannel {

    public static final String FILE_NAME = "faction.conf";

    private transient @MonotonicNonNull @Inject CarbonMessages messages;
    private transient @MonotonicNonNull @Inject UserManager<?> users;

    public FactionChannel() {
        this.key = Key.key("carbon", "faction");
        this.commandAliases = List.of("fc");

        this.messageSource = new ConfigChannelMessageSource();
        this.messageSource.defaults = Map.of(
            "default_format", "(faction: %factionsuuid_faction_name%) <display_name>: <message>",
            "console", "[faction: %factionsuuid_faction_name%] <username>: <message>"
        );
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer player) {
        return channelPermissionResult(
            this.faction(player) != null,
            () -> this.messages.cannotUseFactionChannel(player)
        );
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
        return channelPermissionResult(
            this.faction(player) != null,
            () -> this.messages.cannotUseFactionChannel(player)
        );
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        final @Nullable Faction faction = this.faction(sender);

        if (faction == null) {
            if (sender.online()) {
                sender.sendMessage(this.messages.cannotUseFactionChannel(sender));
            }

            return Collections.emptyList();
        }

        final List<Audience> recipients = new ArrayList<>();
        for (final Player player : faction.getOnlinePlayers()) {
            final @Nullable CarbonPlayer carbon = this.users.user(player.getUniqueId()).getNow(null);
            if (carbon != null) {
                recipients.add(carbon);
            }
        }

        recipients.add(this.server.console());

        return recipients;
    }

    private @Nullable Faction faction(final CarbonPlayer player) {
        final FPlayer fPlayer = FPlayers.getInstance().getById(player.uuid().toString());

        if (fPlayer == null || !fPlayer.hasFaction()) {
            return null;
        }

        return fPlayer.getFaction();
    }

}

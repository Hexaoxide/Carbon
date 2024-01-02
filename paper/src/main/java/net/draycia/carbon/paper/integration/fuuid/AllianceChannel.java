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

import com.google.inject.Inject;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.perms.Relation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.draycia.carbon.api.channels.ChannelPermissionResult;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
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
public class AllianceChannel extends AbstractFactionsChannel {

    public static final String FILE_NAME = "factionsuuid-alliancechat.conf";

    private transient @MonotonicNonNull @Inject CarbonMessages messages;
    private transient @MonotonicNonNull @Inject UserManager<?> users;

    public AllianceChannel() {
        this.key = Key.key("carbon", "alliancechat");
        this.commandAliases = List.of("ac");

        this.messageSource = new ConfigChannelMessageSource();
        this.messageSource.defaults = Map.of(
            "default_format", "(alliance: %factionsuuid_faction_name%) <display_name>: <message>",
            "console", "[alliance: %factionsuuid_faction_name%] <username>: <message>"
        );
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer player) {
        return channelPermissionResult(
            this.hasRelations(player, Relation.ALLY),
            () -> this.messages.cannotUseFactionAllianceChannel(player)
        );
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
        return channelPermissionResult(
            this.hasRelations(player, Relation.ALLY),
            () -> this.messages.cannotUseFactionAllianceChannel(player)
        );
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        if (!this.hasRelations(sender, Relation.ALLY)) {
            if (sender.online()) {
                sender.sendMessage(this.messages.cannotUseFactionAllianceChannel(sender));
            }

            return Collections.emptyList();
        }

        final List<Audience> recipients = new ArrayList<>();
        for (final Player player : this.alliedPlayersTo(sender)) {
            final @Nullable CarbonPlayer carbon = this.users.user(player.getUniqueId()).getNow(null);
            if (carbon != null) {
                recipients.add(carbon);
            }
        }

        recipients.add(this.server.console());

        return recipients;
    }

    private List<Player> alliedPlayersTo(final CarbonPlayer player) {
        final @Nullable Faction faction = this.faction(player);

        if (faction == null) {
            return List.of();
        }

        final List<Player> alliedPlayers = new ArrayList<>();

        for (final FPlayer onlinePlayer : FPlayers.getInstance().getOnlinePlayers()) {
            final Relation relation = faction.getRelationTo(onlinePlayer);

            if (relation.isAtLeast(Relation.ALLY)) {
                alliedPlayers.add(onlinePlayer.getPlayer());
            }
        }

        return alliedPlayers;
    }

}

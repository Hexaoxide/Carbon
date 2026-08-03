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
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.perms.Role;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.draycia.carbon.api.channels.ChannelPermissions;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import static net.draycia.carbon.api.channels.ChannelPermissionResult.channelPermissionResult;

@NullMarked
@ConfigSerializable
public class FactionModChannel extends AbstractFactionsChannel {

    public static final String FILE_NAME = "factionsuuid-factionmodchat.conf";

    private transient @MonotonicNonNull @Inject UserManager<?> users;

    // We could check if the player doesn't have the normal role, but this list may be configurable in the future?
    private transient final List<Role> validRoles = List.of(Role.ADMIN, Role.MODERATOR, Role.COLEADER);

    public FactionModChannel() {
        this.key = Key.key("carbon", "factionmodchat");
        this.commandAliases = List.of("mc");

        this.messageSource = new ConfigChannelMessageSource();
        this.messageSource.defaults = Map.of(
            "default_format", "(fmod: %factionsuuid_faction_name%) <display_name>: <message>",
            "console", "[fmod: %factionsuuid_faction_name%] <username>: <message>"
        );
    }

    @Override
    public ChannelPermissions permissions() {
        return ChannelPermissions.uniformDynamic(player -> channelPermissionResult(
            this.validRoles.contains(this.factionRole(player)),
            () -> this.messages.cannotUseFactionModChannel(player)
        ));
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        if (!this.validRoles.contains(this.factionRole(sender))) {
            if (sender.online()) {
                sender.sendMessage(this.messages.cannotUseFactionModChannel(sender));
            }

            return Collections.emptyList();
        }

        final List<Audience> recipients = new ArrayList<>();
        for (final Player player : this.factionMods(sender)) {
            final @Nullable CarbonPlayer carbon = this.users.user(player.getUniqueId()).getNow(null);
            if (carbon != null) {
                recipients.add(carbon);
            }
        }

        recipients.add(this.server.console());

        return recipients;
    }

    private List<Player> factionMods(final CarbonPlayer player) {
        final @Nullable Faction faction = this.faction(player);

        if (faction == null) {
            return List.of();
        }

        final List<Player> factionMods = new ArrayList<>();

        for (final FPlayer onlinePlayer : faction.getFPlayersWhereOnline(true)) {
            if (this.validRoles.contains(onlinePlayer.getRole())) {
                factionMods.add(onlinePlayer.getPlayer());
            }
        }

        return factionMods;
    }

}

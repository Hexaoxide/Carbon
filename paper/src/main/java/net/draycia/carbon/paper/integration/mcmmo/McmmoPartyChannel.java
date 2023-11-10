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
package net.draycia.carbon.paper.integration.mcmmo;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.party.PartyManager;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public class McmmoPartyChannel extends ConfigChatChannel {

    public static final String FILE_NAME = "mcmmochat.conf";

    private transient @MonotonicNonNull @Inject CarbonMessages messages;
    private transient @MonotonicNonNull @Inject UserManager<?> users;

    public McmmoPartyChannel() {
        // TODO: log warning if normal party chat is also enabled
        this.key = Key.key("carbon", "party");
        this.commandAliases = List.of("pc", "partychat");

        this.messageSource = new ConfigChannelMessageSource();
        this.messageSource.defaults = Map.of(
            "default_format", "(party: %mcmmo_party_name%) <display_name>: <message>",
            "console", "[party: %mcmmo_party_name%] <username> - <uuid>: <message>"
        );
        this.messageSource.locales = Map.of(
            Locale.US, Map.of("default_format", "(party: %mcmmo_party_name%) <display_name>: <message>")
        );
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer player) {
        return this.party(player) != null
            ? ChannelPermissionResult.allowed()
            : ChannelPermissionResult.denied(Component.empty());
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
        return this.party(player) != null
            ? ChannelPermissionResult.allowed()
            : ChannelPermissionResult.denied(Component.empty());
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        final @Nullable Party party = this.party(sender);

        if (party == null) {
            if (sender.online()) {
                this.messages.cannotUseMcmmoPartyChannel(sender);
            }

            return Collections.emptyList();
        }

        final List<Audience> recipients = new ArrayList<>();
        for (final Player player : party.getOnlineMembers()) {
            final @Nullable CarbonPlayer carbon = this.users.user(player.getUniqueId()).getNow(null);
            if (carbon != null) {
                recipients.add(carbon);
            }
        }

        recipients.add(this.server.console());

        return recipients;
    }

    private @Nullable Party party(final CarbonPlayer player) {
        return PartyManager.getParty(Bukkit.getPlayer(player.uuid()));
    }

}

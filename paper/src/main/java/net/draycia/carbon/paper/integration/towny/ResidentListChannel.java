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
package net.draycia.carbon.paper.integration.towny;

import com.google.inject.Inject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.ResidentList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.draycia.carbon.api.channels.ChannelPermissions;
import net.draycia.carbon.api.channels.RecipientsResolver;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.api.channels.ChannelPermissionResult.channelPermissionResult;

@DefaultQualifier(NonNull.class)
abstract class ResidentListChannel<T extends ResidentList> extends ConfigChatChannel {

    protected static final String TOWNY_CHANNEL_HEADER = """
        See the Towny Wiki at https://github.com/TownyAdvanced/Towny/wiki/Placeholders
        for placeholders Towny provides to PlaceholderAPI.
        """;
    protected final static TownyAPI TOWNY_API = TownyAPI.getInstance();

    protected transient @MonotonicNonNull @Inject UserManager<?> users;

    protected abstract @Nullable T residentList(CarbonPlayer player);

    @Override
    public ChannelPermissions permissions() {
        return ChannelPermissions.uniformDynamic(player -> channelPermissionResult(
            this.residentList(player) != null,
            () -> this.cannotUseChannel(player)
        ));
    }

    @Override
    public RecipientsResolver recipientsResolver() {
        return sender -> {
            final @Nullable T residentList = this.residentList(sender);

            if (residentList == null) {
                if (sender.online()) {
                    sender.sendMessage(this.cannotUseChannel(sender));
                }

                return Collections.emptyList();
            }

            final List<Audience> recipients = new ArrayList<>();
            for (final Player player : this.onlinePlayers(residentList)) {
                final @Nullable CarbonPlayer carbon = this.users.user(player.getUniqueId()).getNow(null);
                if (carbon != null) {
                    recipients.add(carbon);
                }
            }

            recipients.add(this.server.console());

            return recipients;
        };
    }

    protected List<Player> onlinePlayers(final T residentList) {
        return TOWNY_API.getOnlinePlayers(residentList);
    }

    protected abstract Component cannotUseChannel(CarbonPlayer player);

}

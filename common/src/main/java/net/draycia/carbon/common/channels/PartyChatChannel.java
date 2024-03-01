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
package net.draycia.carbon.common.channels;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChannelPermissionResult;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messages.SourcedAudience;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import static net.draycia.carbon.api.channels.ChannelPermissionResult.channelPermissionResult;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public class PartyChatChannel extends ConfigChatChannel {

    public static final String FILE_NAME = "partychat.conf";

    private transient @MonotonicNonNull @Inject CarbonMessages messages;

    public PartyChatChannel() {
        this.key = Key.key("carbon", "partychat");
        this.commandAliases = List.of("pc");
        this.messageSource = new ConfigChannelMessageSource();
        this.messageSource.defaults = Map.of(
            "default_format", "(party: <party_name>) <display_name>: <message>",
            "console", "[party: <party_name>] <username>: <message>"
        );
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer player) {
        return channelPermissionResult(
            player.party().join() != null,
            () -> this.messages.cannotUsePartyChannel(player)
        );
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
        return channelPermissionResult(
            player.party().join() != null,
            () -> this.messages.cannotUsePartyChannel(player)
        );
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        final WrappedCarbonPlayer wrapped = (WrappedCarbonPlayer) sender;
        final @Nullable UUID party = wrapped.partyId();
        if (party == null) {
            if (sender.online()) {
                sender.sendMessage(this.messages.cannotUsePartyChannel(sender));
            }
            return new ArrayList<>();
        }
        final List<Audience> recipients = super.recipients(sender);
        recipients.removeIf(r -> r instanceof WrappedCarbonPlayer p && !Objects.equals(p.partyId(), party));
        return recipients;
    }

    @Override
    public @NotNull Component render(
        final CarbonPlayer sender,
        final Audience recipient,
        final Component message,
        final Component originalMessage
    ) {
        final @Nullable Party party = sender.party().join();
        return this.carbonMessages().chatFormat(
            SourcedAudience.of(sender, recipient),
            sender.uuid(),
            this.key(),
            sender.displayName(),
            sender.username(),
            message,
            party == null ? Component.text("null") : party.name()
        );
    }

}

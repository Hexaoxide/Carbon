/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@Singleton
@DefaultQualifier(NonNull.class)
public final class BasicChatChannel implements ChatChannel {

    private final Key key = Key.key("carbon", "basic");

    private final CarbonMessages service;
    private final CarbonChat carbonChat;

    @Inject
    private BasicChatChannel(
        final CarbonMessages service,
        final CarbonChat carbonChat
    ) {
        this.carbonChat = carbonChat;
        this.service = service;
    }

    @Override
    public @NotNull RenderedMessage render(
        final CarbonPlayer sender,
        final Audience recipient,
        final Component message,
        final Component originalMessage,
        final ChatChannel channel
    ) {
        return this.service.basicChatFormat(
            recipient,
            sender.uuid(),
            CarbonPlayer.renderName(sender),
            sender.username(),
            message
        );
    }

    @Override
    public @Nullable String quickPrefix() {
        return null;
    }

    @Override
    public boolean shouldRegisterCommands() {
        return false;
    }

    @Override
    public String commandName() {
        return "basic";
    }

    @Override
    public List<String> commandAliases() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable String permission() {
        return null;
    }

    @Override
    public double radius() {
        return -1;
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer carbonPlayer) {
        return ChannelPermissionResult.allowed();
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
        return ChannelPermissionResult.allowed();
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        final List<Audience> recipients = new ArrayList<>();

        for (final CarbonPlayer player : this.carbonChat.server().players()) {
            if (this.hearingPermitted(player).permitted()) {
                recipients.add(player);
            }
        }

        // console too!
        recipients.add(this.carbonChat.server().console());

        return recipients;
    }

    @Override
    public Set<CarbonPlayer> filterRecipients(final CarbonPlayer sender, final Set<CarbonPlayer> recipients) {
        recipients.removeIf(it -> !this.hearingPermitted(it).permitted());

        return recipients;
    }

    @Override
    public @NonNull Key key() {
        return this.key;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BasicChatChannel otherChannel)) return false;
        if (!(otherChannel.commandName().equals(this.commandName()))) return false;
        if (!(Objects.equals(otherChannel.quickPrefix(), this.quickPrefix()))) return false;
        if (!(Objects.equals(otherChannel.permission(), this.permission()))) return false;
        if (otherChannel.radius() != this.radius()) return false;

        return otherChannel.key().equals(this.key());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.commandName(), this.quickPrefix(), this.permission(), this.radius(), this.key());
    }
}

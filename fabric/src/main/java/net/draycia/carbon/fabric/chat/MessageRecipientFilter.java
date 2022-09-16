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
package net.draycia.carbon.fabric.chat;

import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MessageRecipientFilter {

    private final ServerPlayer sender;
    private final @MonotonicNonNull ChatChannel channel;

    public MessageRecipientFilter(final ServerPlayer sender, final @Nullable ChatChannel channel) {
        this.sender = sender;
        this.channel = channel;
    }

    public boolean shouldFilterMessageTo(final ServerPlayer serverPlayer) {
        final ComponentPlayerResult<? extends CarbonPlayer> authorResult = CarbonChatProvider.carbonChat().server().userManager().carbonPlayer(this.sender.getUUID()).join();
        final ComponentPlayerResult<? extends CarbonPlayer> recipientResult = CarbonChatProvider.carbonChat().server().userManager().carbonPlayer(serverPlayer.getUUID()).join();

        final @MonotonicNonNull CarbonPlayer author = authorResult.player();
        final @MonotonicNonNull CarbonPlayer recipient = recipientResult.player();

        if (author == null || recipient == null) {
            return true; // TODO: should we filter messages when this happens?
        }

        if (author.muted()) {
            return true;
        }

        if (author.ignoring(recipient) || recipient.ignoring(author)) {
            return true;
        }

        if (this.channel == null) {
            return false;
        }

        return !this.channel.hearingPermitted(recipient).permitted();
    }

}

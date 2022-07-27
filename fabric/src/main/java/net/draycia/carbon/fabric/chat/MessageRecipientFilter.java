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

    public MessageRecipientFilter(final ServerPlayer sender, @Nullable ChatChannel channel) {
        this.sender = sender;
        this.channel = channel;
    }

    public boolean shouldFilterMessageTo(final ServerPlayer serverPlayer) {
        final ComponentPlayerResult<? extends CarbonPlayer> authorResult = CarbonChatProvider.carbonChat().server().userManager().carbonPlayer(sender.getUUID()).join();
        final ComponentPlayerResult<? extends CarbonPlayer> recipientResult = CarbonChatProvider.carbonChat().server().userManager().carbonPlayer(serverPlayer.getUUID()).join();

        final @MonotonicNonNull CarbonPlayer author = authorResult.player();
        final @MonotonicNonNull CarbonPlayer recipient = recipientResult.player();

        if (author == null || recipient == null) {
            return true; // TODO: should we filter messages when this happens?
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

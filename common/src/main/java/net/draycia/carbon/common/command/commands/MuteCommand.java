package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.arguments.CarbonPlayerArgument;
import net.draycia.carbon.common.messages.CarbonMessageService;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MuteCommand {

    @Inject
    public MuteCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonChat carbonChat,
        final CarbonPlayerArgument carbonPlayerArgument
    ) {
        var command = commandManager.commandBuilder("mute")
            .argument(carbonPlayerArgument.newInstance(true, "player"))
            .permission("carbon.mute.mute") // TODO: carbon.whisper.spy
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander)handler.getSender()).carbonPlayer();
                final CarbonPlayer target = handler.get("player");

                if (target.hasPermission("carbon.mute.exempt")) {
                    messageService.muteExempt(sender);
                    return;
                }

                final @Nullable ChatChannel channel = handler.flags().get("channel");
                final long duration =  handler.flags().getValue("duration", -1L);
                final @Nullable String reason = handler.flags().get("reason");

                // Intentionally directly pass in the flags, they're null if not present
                target.addMuteEntry(channel, true, target.uuid(), duration, reason);

                for (final var player : carbonChat.server().players()) {
                    if (!player.equals(sender) && !player.hasPermission("carbon.mute.notify")) {
                        continue;
                    }

                    sendMuteMessage(messageService, sender, target, player, reason, channel, duration);
                }
            })
            .build();

        commandManager.command(command);
    }

    private void sendMuteMessage(
        final CarbonMessageService messageService,
        final CarbonPlayer sender,
        final CarbonPlayer target,
        final CarbonPlayer recipient,
        final @Nullable String reason,
        final @Nullable ChatChannel channel,
        final long duration
    ) {
        if (target.equals(recipient)) {
            // TODO: "you have been muted"
        }

        // Oh no
        if (reason != null && target.hasPermission("carbon.mute.notify.reason")) {
            if (channel != null) {
                if (duration == -1) {
                    messageService.broadcastPlayerChannelMutedPermanentlyReason(recipient, target.username(),
                        sender.username(), reason, channel);
                } else {
                    messageService.broadcastPlayerChannelMutedDurationReason(recipient,target.username(),
                        sender.username(), reason, channel, duration);
                }
            } else {
                if (duration == -1) {
                    messageService.broadcastPlayerMutedPermanentlyReason(recipient, target.username(),
                        sender.username(), reason);
                } else {
                    messageService.broadcastPlayerMutedDurationReason(recipient, sender.username(),
                        reason, target.username(), duration);
                }
            }
        } else {
            if (channel != null) {
                if (duration == -1) {
                    messageService.broadcastPlayerChannelMutedPermanently(recipient, target.username(),
                        sender.username(), channel);
                } else {
                    messageService.broadcastPlayerChannelMutedDuration(recipient, target.username(),
                        sender.username(), channel, duration);
                }
            } else {
                if (duration == -1) {
                    messageService.broadcastPlayerMutedPermanently(recipient, sender.username(),
                        target.username());
                } else {
                    messageService.broadcastPlayerMutedDuration(recipient, sender.username(),
                        target.username(), duration);
                }
            }
        }
    }

}

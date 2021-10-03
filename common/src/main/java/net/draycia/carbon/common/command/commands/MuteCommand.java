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
            .permission("carbon.mute.") // TODO: carbon.whisper.spy
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

                // TODO: tell sender "player is now muted"
                // TODO: tell target "you are now muted"

                // TODO: broadcast to mods "player is now muted"
                for (final var player : carbonChat.server().players()) {
                    if (!player.hasPermission("carbon.mute.notify")) {
                        continue;
                    }

                    // oh no
                    if (reason != null && player.hasPermission("carbon.mute.notify.reason")) {
                        if (channel != null) {
                            if (duration == -1) {
                                messageService.broadcastPlayerChannelMutedPermanentlyReason(player, target.username(),
                                    sender.username(), reason, channel);
                            } else {
                                messageService.broadcastPlayerChannelMutedDurationReason(player,target.username(),
                                    sender.username(), reason, channel, duration);
                            }
                        } else {
                            if (duration == -1) {
                                messageService.broadcastPlayerMutedPermanentlyReason(player, target.username(),
                                    sender.username(), reason);
                            } else {
                                messageService.broadcastPlayerMutedDurationReason(player, sender.username(),
                                    reason, target.username(), duration);
                            }
                        }
                    } else {
                        if (channel != null) {
                            if (duration == -1) {
                                messageService.broadcastPlayerChannelMutedPermanently(player, target.username(),
                                    sender.username(), channel);
                            } else {
                                messageService.broadcastPlayerChannelMutedDuration(player, target.username(),
                                    sender.username(), channel, duration);
                            }
                        } else {
                            if (duration == -1) {
                                messageService.broadcastPlayerMutedPermanently(player, sender.username(),
                                    target.username());
                            } else {
                                messageService.broadcastPlayerMutedDuration(player, sender.username(),
                                    target.username(), duration);
                            }
                        }
                    }
                }
            })
            .build();

        commandManager.command(command);
    }

}

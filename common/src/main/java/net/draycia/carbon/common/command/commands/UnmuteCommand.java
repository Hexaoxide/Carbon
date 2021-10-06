package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.arguments.CarbonPlayerArgument;
import net.draycia.carbon.common.messages.CarbonMessageService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class UnmuteCommand {

    @Inject
    public UnmuteCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonChat carbonChat,
        final CarbonPlayerArgument carbonPlayerArgument
    ) {
        final var command = commandManager.commandBuilder("unmute")
            .argument(carbonPlayerArgument.newInstance(true, "player"))
            .permission("carbon.mute.unmute")
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander)handler.getSender()).carbonPlayer();
                final CarbonPlayer target = handler.get("player");

                messageService.playerAlertUnmuted(target);
                messageService.broadcastPlayerUnmuted(sender, target.username());

                for (final var player : carbonChat.server().players()) {
                    if (player.equals(sender) || player.equals(target)) {
                        continue;
                    }

                    if (!player.hasPermission("carbon.mute.notify")) {
                        continue;
                    }

                    messageService.broadcastPlayerUnmuted(player, target.username());
                }
            })
            .build();

        commandManager.command(command);
    }

}

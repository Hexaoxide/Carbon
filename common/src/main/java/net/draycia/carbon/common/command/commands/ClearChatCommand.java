package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.PrimaryConfig;

public class ClearChatCommand {

    @Inject
    public ClearChatCommand(
        final CarbonChat carbonChat,
        final CommandManager<Commander> commandManager,
        final PrimaryConfig config
    ) {
        final var command = commandManager.commandBuilder("clearchat", "chatclear", "cc")
            .permission("carbon.clearchat")
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                // Not fond of having to send 50 messages to each player
                // Are we not able to just paste in 50 newlines and call it a day?
                for (int i = 0; i < config.clearChatSettings().iterations(); i++) {
                    for (final var player : carbonChat.server().players()) {
                        if (!player.hasPermission("carbon.clearchat.exempt")) {
                            player.sendMessage(config.clearChatSettings().message());
                        }
                    }
                }

                carbonChat.server().sendMessage(config.clearChatSettings().broadcast());
            })
            .build();

        commandManager.command(command);
    }

}

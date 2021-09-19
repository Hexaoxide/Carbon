package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import com.google.inject.Inject;
import com.google.inject.Injector;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.arguments.CarbonPlayerArgument;
import net.draycia.carbon.common.messages.CarbonMessageService;

public class WhisperCommand {

    @Inject
    public WhisperCommand(
        final Injector injector,
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonPlayerArgument carbonPlayerArgument
    ) {
        var command = commandManager.commandBuilder("whisper", "w", "message", "msg")
            .argument(carbonPlayerArgument.newInstance(true, "recipient"))
            .argument(StringArgument.greedy("message"))
            .permission("carbon.whisper.send") // TODO: carbon.whisper.spy
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander)handler.getSender()).carbonPlayer();

                final String message = handler.get("message");
                final CarbonPlayer recipient = handler.get("recipient");

                if (sender.equals(recipient)) {
                    // TODO: send error message "you cannot whisper yourself!"
                    //return;
                }

                messageService.whisperSender(new SourcedAudience(sender, sender),
                    sender.displayName(), recipient.displayName(), message);

                messageService.whisperRecipient(new SourcedAudience(sender, recipient),
                    sender.displayName(), recipient.displayName(), message);

                sender.whisperReplyTarget(recipient.uuid());
                recipient.whisperReplyTarget(sender.uuid());
            })
            .build();

        commandManager.command(command);
    }

}

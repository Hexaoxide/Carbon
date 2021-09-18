package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import com.google.inject.Inject;
import com.google.inject.Injector;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.messages.CarbonMessageService;

public class WhisperCommand {

    @Inject
    public WhisperCommand(
        final Injector injector,
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService
        ) {
        var command = commandManager.commandBuilder("whisper", "w", "message", "msg")
            .argument(StringArgument.greedy("message"))
            .argument(/* Audience recipient argument */)
            .permission("carbon.whisper.send") // TODO: carbon.whisper.spy
            .senderType(CarbonPlayer.class)
            .handler(handler -> {
                final String message = handler.get("message");
                final CarbonPlayer sender = (CarbonPlayer) handler.getSender();
                final CarbonPlayer recipient = handler.get("recipient");

                if (sender.equals(recipient)) {
                    // TODO: send error message "you cannot whisper yourself!"
                    return;
                }

                messageService.whisperSender(new SourcedAudience(handler.getSender(), handler.getSender()),
                    sender.displayName(), recipient.displayName());

                messageService.whisperRecipient(new SourcedAudience(handler.getSender(), recipient),
                    sender.displayName(), recipient.displayName());

                sender.whisperReplyTarget(recipient.uuid());
                recipient.whisperReplyTarget(sender.uuid());
            })
            .build();

        commandManager.command(command);
    }

}

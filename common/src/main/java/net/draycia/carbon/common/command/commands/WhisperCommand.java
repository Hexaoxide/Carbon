package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import com.google.inject.Inject;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.messages.CarbonMessageService;

public class WhisperCommand {

    @Inject
    public WhisperCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonPlayerArgument carbonPlayerArgument
    ) {
        var command = commandManager.commandBuilder("whisper", "w", "message", "msg")
            .argument(carbonPlayerArgument.newInstance(true, "recipient", CarbonPlayerArgument.NO_SENDER))
            .argument(StringArgument.greedy("message"))
            .permission("carbon.whisper.send") // TODO: carbon.whisper.spy
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander)handler.getSender()).carbonPlayer();

                final String message = handler.get("message");
                final CarbonPlayer recipient = handler.get("recipient");

                if (sender.equals(recipient)) {
                    messageService.whisperSelfError(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!recipient.online()) {
                    messageService.whisperTargetOffline(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!sender.awareOf(recipient) && !sender.hasPermission("carbon.seevanish.whisper")) {
                    messageService.whisperTargetOffline(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                messageService.whisperSender(new SourcedAudience(sender, sender),
                    CarbonPlayer.renderName(sender), CarbonPlayer.renderName(recipient), message);

                messageService.whisperRecipient(new SourcedAudience(sender, recipient),
                    CarbonPlayer.renderName(sender), CarbonPlayer.renderName(recipient), message);

                sender.lastWhisperTarget(recipient.uuid());
                sender.whisperReplyTarget(recipient.uuid());
                recipient.whisperReplyTarget(sender.uuid());
            }) // TODO: let command name and aliases be configurable, because why not
            .build();

        commandManager.command(command);
    }

}

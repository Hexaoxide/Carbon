package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import com.google.inject.Inject;
import java.util.UUID;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.SourcedAudience;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessageService;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class ContinueCommand {

    @Inject
    public ContinueCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonChat carbonChat
    ) {
        var command = commandManager.commandBuilder("continue", "c")
            .argument(StringArgument.greedy("message"))
            .permission("carbon.whisper.continue") // TODO: carbon.whisper.spy
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander)handler.getSender()).carbonPlayer();

                final String message = handler.get("message");
                final UUID whisperTarget = sender.lastWhisperTarget();

                if (whisperTarget == null) {
                    messageService.whisperTargetNotSet(sender, sender.displayName());
                    return;
                }

                final ComponentPlayerResult result = carbonChat.server().player(whisperTarget).join();
                final @MonotonicNonNull CarbonPlayer recipient = result.player();

                if (recipient == null) {
                    messageService.whisperTargetOffline(sender, sender.displayName());
                    return;
                }

                if (sender.equals(recipient)) {
                    messageService.whisperSelfError(sender, sender.displayName());
                    return;
                }

                messageService.whisperSender(new SourcedAudience(sender, sender),
                    sender.displayName(), recipient.displayName(), message);

                messageService.whisperRecipient(new SourcedAudience(sender, recipient),
                    sender.displayName(), recipient.displayName(), message);

                sender.lastWhisperTarget(recipient.uuid());
                sender.whisperReplyTarget(recipient.uuid());
                recipient.whisperReplyTarget(sender.uuid());
            })
            .build();

        commandManager.command(command);
    }

}

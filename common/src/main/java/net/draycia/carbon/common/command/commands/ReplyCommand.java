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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ReplyCommand {

    @Inject
    public ReplyCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonChat carbonChat
        ) {
        var command = commandManager.commandBuilder("reply", "r")
            .argument(StringArgument.greedy("message"))
            .permission("carbon.whisper.reply") // TODO: carbon.whisper.spy
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander)handler.getSender()).carbonPlayer();

                final String message = handler.get("message");
                final @Nullable UUID replyTarget = sender.whisperReplyTarget();

                if (replyTarget == null) {
                    messageService.replyTargetNotSet(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                final ComponentPlayerResult<CarbonPlayer> result = carbonChat.server().player(replyTarget).join();
                final @MonotonicNonNull CarbonPlayer recipient = result.player();

                if (recipient == null) {
                    messageService.replyTargetOffline(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!sender.awareOf(recipient) && !sender.hasPermission("carbon.seevanish.reply")) {
                    messageService.whisperTargetOffline(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (sender.equals(recipient)) {
                    messageService.whisperSelfError(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                if (!recipient.online()) {
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
            })
            .build();

        commandManager.command(command);
    }

}

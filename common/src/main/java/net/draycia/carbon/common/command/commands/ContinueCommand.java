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
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.draycia.carbon.common.util.CloudUtils;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ContinueCommand {

    @Inject
    public ContinueCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonChat carbonChat
    ) {
        final var command = commandManager.commandBuilder("continue", "c")
            .argument(StringArgument.greedy("message"))
            .permission("carbon.whisper.continue") // TODO: carbon.whisper.spy
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();

                final String message = handler.get("message");
                final UUID whisperTarget = sender.lastWhisperTarget();

                if (whisperTarget == null) {
                    messageService.whisperTargetNotSet(sender, CarbonPlayer.renderName(sender));
                    return;
                }

                final ComponentPlayerResult<@NonNull CarbonPlayer> result = carbonChat.server()
                    .player(whisperTarget).join();
                final @MonotonicNonNull CarbonPlayer recipient = result.player();

                if (!recipient.online()
                    || (!sender.awareOf(recipient)
                    && !sender.hasPermission("carbon.seevanish.whisper"))
                ) {
                    final var rawNameInput = CloudUtils.rawInputByMatchingName(handler.getRawInput(), recipient);
                    final var exception = new CarbonPlayerArgument.PlayerParseException(rawNameInput);

                    messageService.errorCommandArgumentParsing(sender, CloudUtils.message(exception));
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

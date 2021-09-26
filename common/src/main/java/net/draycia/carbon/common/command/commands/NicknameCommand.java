package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.permission.Permission;
import com.google.inject.Inject;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.arguments.CarbonPlayerArgument;
import net.draycia.carbon.common.command.arguments.OptionValueParser;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class NicknameCommand {

    @Inject
    public NicknameCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonPlayerArgument carbonPlayerArgument
    ) {
        var command = commandManager.commandBuilder("nickname", "nick")
            .argument(StringArgument.greedy("nickname"))
            //.flag(commandManager.flagBuilder("duration")
            //    .withAliases("d")
            //    .withArgument()
            //    .withPermission(Permission.of("carbon.nickname.duration"))
            //)
            .flag(commandManager.flagBuilder("player")
                .withAliases("p")
                .withArgument(carbonPlayerArgument.newInstance(true, "recipient"))
                .withPermission(Permission.of("carbon.nickname.others"))
            )
            .flag(commandManager.flagBuilder("nickname")
                .withAliases("n")
                .withArgument(StringArgument.newBuilder("nickname").greedy().withParser(new OptionValueParser<>()))
                .withPermission(Permission.of("carbon.nickname.set"))
            )
            .permission("carbon.nickname.self")
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                CarbonPlayer sender = ((PlayerCommander)handler.getSender()).carbonPlayer();
                long expirationTime = -1; // TODO: implement timed nicknames

                if (handler.contains("duration")) {
                    expirationTime = handler.get("duration");
                }

                // Setting nickname
                if (handler.contains("nickname")) {
                    final var nickname = MiniMessage.get().parse(handler.get("nickname"));

                    // Setting other player's nickname
                    if (handler.contains("player")) {
                        final CarbonPlayer target = handler.get("player");
                        target.displayName(nickname);
                        messageService.nicknameSet(target, nickname);
                        messageService.nicknameSetOthers(sender, target.username(), nickname);
                    } else {
                        // Setting own nickname
                        sender.displayName(nickname);
                        messageService.nicknameSet(sender, nickname);
                    }
                } else if (handler.contains("player")) {
                    // Checking other player's nickname
                    final CarbonPlayer target = handler.get("player");

                    if (target.displayName() != null) {
                        messageService.nicknameShowOthers(sender, target.username(), target.displayName());
                    } else {
                        messageService.nicknameShowOthersUnset(sender, target.username());
                    }
                } else {
                    // Checking own nickname
                    if (sender.displayName() != null) {
                        messageService.nicknameShow(sender, sender.username(), sender.displayName());
                    } else {
                        messageService.nicknameShowUnset(sender, sender.username());
                    }
                }
            })
            .build();

        commandManager.command(command);
    }

}

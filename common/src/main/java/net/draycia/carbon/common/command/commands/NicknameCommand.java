package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.permission.Permission;
import com.google.inject.Inject;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.arguments.CarbonPlayerArgument;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.text.Component;
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
            .flag(commandManager.flagBuilder("player")
                .withAliases("p", "target", "t")
                .withArgument(carbonPlayerArgument.newInstance(true, "recipient"))
                .withPermission(Permission.of("carbon.nickname.others"))
            )
            .permission("carbon.nickname.self")
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                CarbonPlayer sender = ((PlayerCommander)handler.getSender()).carbonPlayer();
                final var nickname = MiniMessage.get().parse(handler.get("nickname"));

                if (handler.contains("player")) {
                    final CarbonPlayer target = handler.get("player");
                    target.displayName(nickname);
                    messageService.nicknameSet(target, nickname);

                    if (target.hasCustomDisplayName()) {
                        messageService.nicknameSetOthers(sender, target.displayName(), nickname);
                    } else {
                        messageService.nicknameSetOthers(sender, Component.text(target.username()), nickname);
                    }

                    return;
                }

                sender.displayName(nickname);
                messageService.nicknameSet(sender, nickname);
            })
            .build();

        commandManager.command(command);
    }

}

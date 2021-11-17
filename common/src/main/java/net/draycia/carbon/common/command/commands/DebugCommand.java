package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import com.google.inject.Inject;
import java.util.ArrayList;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.CarbonPlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public class DebugCommand {

    @Inject
    public DebugCommand(
        final CommandManager<Commander> commandManager,
        final CarbonPlayerArgument carbonPlayerArgument
    ) {
        final var command = commandManager.commandBuilder("carbondebug", "cdebug")
            .argument(carbonPlayerArgument.newInstance(false, "player"))
            .permission("carbon.debug")
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                final CarbonPlayer sender = ((PlayerCommander) handler.getSender()).carbonPlayer();
                final CarbonPlayer target;

                if (handler.contains("player")) {
                    target = handler.get("player");
                } else {
                    target = sender;
                }

                sender.sendMessage(
                    Component.join(JoinConfiguration.noSeparators(),
                        Component.text("Primary Group: ", NamedTextColor.GOLD),
                        Component.text(target.primaryGroup(), NamedTextColor.GREEN))
                );

                final var groups = new ArrayList<Component>();

                for (final var group : target.groups()) {
                    groups.add(Component.text(group, NamedTextColor.GREEN));
                }

                final var formattedGroupsList =
                    Component.join(JoinConfiguration.separator(
                        Component.text(", ", NamedTextColor.YELLOW)), groups
                    );

                sender.sendMessage(
                    Component.join(JoinConfiguration.noSeparators(),
                        Component.text("Groups: ", NamedTextColor.GOLD),
                        formattedGroupsList
                    )
                );
            })
            .build();

        commandManager.command(command);
    }

}

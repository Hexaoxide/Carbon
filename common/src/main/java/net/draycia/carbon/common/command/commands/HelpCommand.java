package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import com.google.inject.Inject;
import net.draycia.carbon.common.command.Commander;

public class HelpCommand {

    @Inject
    public HelpCommand(
        final CommandManager<Commander> commandManager
    ) {
        var command = commandManager.commandBuilder("carbonhelp")
            .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
            .permission("carbon.help")
            .senderType(Commander.class)
            .handler(handler -> {
                var help = new MinecraftHelp<>("/carbonhelp", AudienceProvider.nativeAudience(), commandManager);
                help.queryCommands(handler.getOrDefault("query", ""), handler.getSender());
            })
            .build();

        commandManager.command(command);
    }
}

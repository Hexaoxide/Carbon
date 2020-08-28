package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.List;

public class ChatReloadCommand {

    private final CarbonChat carbonChat;

    public ChatReloadCommand(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        String commandName = carbonChat.getConfig().getString("commands.reload.name", "chatreload");
        List<String> commandAliases = carbonChat.getConfig().getStringList("commands.reload.aliases");

        LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
        channelArguments.put("channel", CarbonUtils.channelArgument());

        new CommandAPICommand(commandName)
                .withArguments(channelArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.reload"))
                .executes(this::execute)
                .register();
    }

    private void execute(CommandSender sender, Object[] args) {
        carbonChat.reloadConfig();

        carbonChat.getChannelManager().reload();

        Component message = carbonChat.getAdventureManager().processMessage(carbonChat.getLanguage().getString("reloaded"),
                "br", "\n");

        carbonChat.getAdventureManager().getAudiences().audience(sender).sendMessage(message);

        // TODO: Ensure the command list is updated when reloading.
        // If new channels are added, their commands should appear to players.

        // TODO: Ensure moderation filters ETC are updated when reloading.
    }

}

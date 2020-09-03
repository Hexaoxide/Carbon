package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;

public class ChatReloadCommand {

    private final CarbonChat carbonChat;

    public ChatReloadCommand(CarbonChat carbonChat, CommandSettings commandSettings) {
        this.carbonChat = carbonChat;

        if (!commandSettings.isEnabled()) {
            return;
        }

        LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
        channelArguments.put("channel", CarbonUtils.channelArgument());

        new CommandAPICommand(commandSettings.getName())
                .withArguments(channelArguments)
                .withAliases(commandSettings.getAliasesArray())
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

package net.draycia.carbon.commands;


import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClearChatCommand {

    private final CarbonChat carbonChat;

    public ClearChatCommand(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        String commandName = carbonChat.getConfig().getString("commands.clearchat.name", "clearchat");
        List<String> commandAliases = carbonChat.getConfig().getStringList("commands.clearchat.aliases");

        new CommandAPICommand(commandName)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.clearchat.clear"))
                .executes(this::execute)
                .register();
    }

    private void execute(CommandSender sender, Object[] args) {
        String format = carbonChat.getModConfig().getString("clear-chat.message", "");
        Component component = carbonChat.getAdventureManager().processMessage(format, "br", "\n");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("carbonchat.clearchat.exempt")) {
                continue;
            }

            ChatUser audience = carbonChat.getUserService().wrap(player);

            for (int i = 0; i < carbonChat.getModConfig().getInt("clear-chat.message-count", 100); i++) {
                audience.sendMessage(component);
            }

            if (player.hasPermission("carbonchat.clearchat.notify")) {
                String message = carbonChat.getLanguage().getString("clear-notify");
                audience.sendMessage(carbonChat.getAdventureManager().processMessage(message, "player", sender.getName()));
            }

            if (player.hasPermission("carbonchat.clearchat.exempt")) {
                String message = carbonChat.getLanguage().getString("clear-exempt");
                audience.sendMessage(carbonChat.getAdventureManager().processMessage(message, "player", sender.getName()));
            }
        }
    }

}

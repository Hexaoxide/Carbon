package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("clearchat|cc")
@CommandPermission("carbonchat.clearchat.clear")
public class ClearChatCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    public void baseCommand(CommandSender issuer) {
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
                String message = carbonChat.getConfig().getString("language.clear-notify");
                audience.sendMessage(carbonChat.getAdventureManager().processMessage(message, "player", issuer.getName()));
            }

            if (player.hasPermission("carbonchat.clearchat.exempt")) {
                String message = carbonChat.getConfig().getString("language.clear-exempt");
                audience.sendMessage(carbonChat.getAdventureManager().processMessage(message, "player", issuer.getName()));
            }
        }
    }

}

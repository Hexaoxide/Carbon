package net.draycia.simplechatmoderation.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechatmoderation.SimpleChatModeration;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("clearchat|cc")
@CommandPermission("simplechat.clearchat.clear")
public class ClearChatCommand extends BaseCommand {

    private SimpleChatModeration moderation;

    public ClearChatCommand(SimpleChatModeration moderation) {
        this.moderation = moderation;
    }

    @Default
    public void baseCommand(CommandSender issuer) {
        String format = moderation.getConfig().getString("clear-chat.message", "");
        Component component = moderation.getSimpleChat().getAdventureManager().processMessage(format, "br", "\n");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("simplechat.clearchat.exempt")) {
                continue;
            }

            Audience audience = moderation.getSimpleChat().getAdventureManager().getAudiences().player(player);

            for (int i = 0; i < moderation.getConfig().getInt("clear-chat.message-count", 100); i++) {
                audience.sendMessage(component);
            }

            if (player.hasPermission("simplechat.clearchat.notify")) {
                String message = moderation.getConfig().getString("language.clear-notify");
                audience.sendMessage(moderation.getSimpleChat().getAdventureManager().processMessage(message, "player", issuer.getName()));
            }

            if (player.hasPermission("simplechat.clearchat.exempt")) {
                String message = moderation.getConfig().getString("language.clear-exempt");
                audience.sendMessage(moderation.getSimpleChat().getAdventureManager().processMessage(message, "player", issuer.getName()));
            }
        }
    }

}

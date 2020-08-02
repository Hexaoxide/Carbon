package net.draycia.carbonmoderation.commands;

import net.draycia.carbon.libs.co.aikar.commands.BaseCommand;
import net.draycia.carbon.libs.co.aikar.commands.annotation.CommandAlias;
import net.draycia.carbon.libs.co.aikar.commands.annotation.CommandPermission;
import net.draycia.carbon.libs.co.aikar.commands.annotation.Default;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbonmoderation.CarbonChatModeration;
import net.draycia.carbon.libs.net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("clearchat|cc")
@CommandPermission("carbonchat.clearchat.clear")
public class ClearChatCommand extends BaseCommand {

    private CarbonChatModeration moderation;

    public ClearChatCommand(CarbonChatModeration moderation) {
        this.moderation = moderation;
    }

    @Default
    public void baseCommand(CommandSender issuer) {
        String format = moderation.getConfig().getString("clear-chat.message", "");
        Component component = moderation.getCarbonChat().getAdventureManager().processMessage(format, "br", "\n");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("carbonchat.clearchat.exempt")) {
                continue;
            }

            ChatUser audience = moderation.getCarbonChat().getUserService().wrap(player);

            for (int i = 0; i < moderation.getConfig().getInt("clear-chat.message-count", 100); i++) {
                audience.sendMessage(component);
            }

            if (player.hasPermission("carbonchat.clearchat.notify")) {
                String message = moderation.getConfig().getString("language.clear-notify");
                audience.sendMessage(moderation.getCarbonChat().getAdventureManager().processMessage(message, "player", issuer.getName()));
            }

            if (player.hasPermission("carbonchat.clearchat.exempt")) {
                String message = moderation.getConfig().getString("language.clear-exempt");
                audience.sendMessage(moderation.getCarbonChat().getAdventureManager().processMessage(message, "player", issuer.getName()));
            }
        }
    }

}

package net.draycia.simplechatmoderation.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechatmoderation.SimpleChatModeration;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("clearchat|cc")
@CommandPermission("simplechat.clearchat.clear")
public class ClearChatCommand extends BaseCommand {

    private SimpleChatModeration moderation;

    public ClearChatCommand(SimpleChatModeration moderation) {
        this.moderation = moderation;
    }

    @Default
    public void baseCommand(CommandIssuer issuer) {
        String format = moderation.getConfig().getString("clear-chat.message", "");
        Component component = MiniMessage.get().parse(format, "br", "\n");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("simplechat.clearchat.exempt")) {
                continue;
            }

            Audience audience = moderation.getSimpleChat().getAudiences().player(player);

            for (int i = 0; i < moderation.getConfig().getInt("clear-chat.message-count", 100); i++) {
                audience.sendMessage(component);
            }

            String name = issuer.isPlayer() ? ((Player)issuer).getName() : "Console";

            if (player.hasPermission("simplechat.clearchat.notify")) {
                String message = moderation.getConfig().getString("language.clear-notify");
                audience.sendMessage(MiniMessage.get().parse(message, "player", name));
            }

            if (player.hasPermission("simplechat.clearchat.exempt")) {
                String message = moderation.getConfig().getString("language.clear-exempt");
                audience.sendMessage(MiniMessage.get().parse(message, "player", name));
            }
        }
    }

}

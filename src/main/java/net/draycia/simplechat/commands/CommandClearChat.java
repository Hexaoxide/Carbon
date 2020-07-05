package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("clearchat|cc")
@CommandPermission("simplechat.clearchat")
public class CommandClearChat extends BaseCommand {

    private SimpleChat simpleChat;

    public CommandClearChat(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    public void baseCommand(CommandIssuer issuer) {
        String format = simpleChat.getConfig().getString("clear-chat-message", "");
        Component component = MiniMessage.get().parse(format);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("simplechat.clearchat.exempt")) {
                continue;
            }

            Audience audience = simpleChat.getAudiences().player(player);

            for (int i = 0; i < simpleChat.getConfig().getInt("clear-chat-amount", 100); i++) {
                audience.sendMessage(component);
            }
        }
    }

}

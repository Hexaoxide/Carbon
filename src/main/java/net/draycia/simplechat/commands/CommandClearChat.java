package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("clearchat|cc")
@CommandPermission("simplechat.clear.use")
public class CommandClearChat extends BaseCommand {

    private SimpleChat simpleChat;

    public CommandClearChat(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    public void baseCommand(CommandIssuer issuer) {
        String format = simpleChat.getConfig().getString("clear-chat-message", "");
        Component component = MiniMessageParser.parseFormat(format);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("simplechat.clear.exempt")) {
                continue;
            }

            Audience audience = simpleChat.getAudiences().player(player);

            for (int i = 0; i < simpleChat.getConfig().getInt("clear-chat-amount", 100); i++) {
                audience.sendMessage(component);
            }
        }
    }

}

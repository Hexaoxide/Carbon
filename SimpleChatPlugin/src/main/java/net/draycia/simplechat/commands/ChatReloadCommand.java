package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

@CommandAlias("chatreload|creload")
@CommandPermission("simplechat.reload")
public class ChatReloadCommand extends BaseCommand {

    @Dependency
    private SimpleChat simpleChat;

    @Default
    public void baseCommand(CommandSender sender) {
        simpleChat.reloadConfig();

        Component message = simpleChat.getAdventureManager().processMessage(simpleChat.getConfig().getString("language.reloaded"),
                "br", "\n");

        simpleChat.getAdventureManager().getAudiences().audience(sender).sendMessage(message);
    }

}

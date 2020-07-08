package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

@CommandAlias("chatreload|creload")
@CommandPermission("simplechat.reload")
public class ChatReloadCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public ChatReloadCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    public void baseCommand(CommandSender sender) {
        simpleChat.reloadConfig();

        // TODO: clear item patterns in SimpleChatChannel

        Component message = MiniMessage.get().parse(simpleChat.getConfig().getString("language.reloaded"));
        simpleChat.getAudiences().audience(sender).sendMessage(message);
    }

}

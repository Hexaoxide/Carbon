package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.entity.Player;

@CommandAlias("msg|whisper|message|w")
@CommandPermission("simplechat.message")
public class MessageCommand extends BaseCommand {

    @Dependency
    private SimpleChat simpleChat;

    @Default
    @CommandCompletion("@players")
    public void baseCommand(Player player, ChatUser targetUser, String... args) {
        if (args.length == 0) {
            return;
        }

        ChatUser sender = simpleChat.getUserService().wrap(player);
        targetUser.sendMessage(sender, String.join(" ", args));
    }

}

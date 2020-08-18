package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.entity.Player;

@CommandAlias("msg|whisper|message|w")
@CommandPermission("carbonchat.message")
public class MessageCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @CommandCompletion("@players")
    @Syntax("<player> <message>")
    public void baseCommand(Player player, ChatUser targetUser, String... args) {
        if (args.length == 0) {
            return;
        }

        ChatUser sender = carbonChat.getUserService().wrap(player);
        targetUser.sendMessage(sender, String.join(" ", args));
    }

}

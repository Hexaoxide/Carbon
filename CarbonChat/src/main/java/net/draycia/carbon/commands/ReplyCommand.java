package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandAlias("reply|r")
@CommandPermission("carbonchat.reply")
public class ReplyCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    public void baseCommand(Player player, String... args) {
        if (args.length == 0) {
            return;
        }

        ChatUser user = carbonChat.getUserService().wrap(player);

        if (user.getReplyTarget() == null) {
            String message = carbonChat.getConfig().getString("language.no-reply-target");
            Component component = carbonChat.getAdventureManager().processMessage(message, "br", "\n");
            user.sendMessage(component);

            return;
        }

        ChatUser targetUser = carbonChat.getUserService().wrap(user.getReplyTarget());

        targetUser.sendMessage(user, String.join(" ", args));
    }

}

package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.managers.UserManager;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("reply|r")
@CommandPermission("simplechat.reply")
public class ReplyCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public ReplyCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    public void baseCommand(Player player, String... args) {
        ChatUser user = UserManager.wrap(player);

        if (user.getReplyTarget() == null) {
            String message = simpleChat.getConfig().getString("language.no-reply-target");
            Component component = MiniMessage.instance().parse(message);
            user.asAudience().sendMessage(component);

            return;
        }

        simpleChat.sendPlayerPrivateMessage(player, Bukkit.getOfflinePlayer(user.getReplyTarget()), String.join(" ", args));
    }

}

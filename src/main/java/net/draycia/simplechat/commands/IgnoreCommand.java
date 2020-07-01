package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("ignore")
@CommandPermission("simplechat.ignore")
public class IgnoreCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public IgnoreCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    @CommandCompletion("@players")
    public void baseCommand(Player player, OfflinePlayer target) {
        ChatUser user = simpleChat.getUserService().wrap(player);
        ChatUser targetUser = simpleChat.getUserService().wrap(target.getUniqueId());

        String message;

        if (user.isIgnoringUser(targetUser)) {
            user.setIgnoringUser(targetUser, false);
            message = simpleChat.getConfig().getString("language.not-ignoring-user");
        } else {
            user.setIgnoringUser(targetUser, true);
            message = simpleChat.getConfig().getString("language.ignoring-user");
        }

        user.asAudience().sendMessage(MiniMessage.instance().parse(message));
    }

}

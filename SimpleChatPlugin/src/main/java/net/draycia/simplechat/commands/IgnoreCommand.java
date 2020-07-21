package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

@CommandAlias("ignore")
@CommandPermission("simplechat.ignore")
public class IgnoreCommand extends BaseCommand {

    @Dependency
    private SimpleChat simpleChat;

    @Default
    @CommandCompletion("@players")
    public void baseCommand(Player player, ChatUser targetUser) {
        ChatUser user = simpleChat.getUserService().wrap(player);

        String message;

        if (user.isIgnoringUser(targetUser)) {
            user.setIgnoringUser(targetUser, false);
            message = simpleChat.getConfig().getString("language.not-ignoring-user");
        } else {
            Permission permission = simpleChat.getPermission();

            if (permission.playerHas(null, targetUser.asOfflinePlayer(), "simplechat.ignore.exempt")) {
                message = simpleChat.getConfig().getString("language.ignore-exempt");
            } else {
                user.setIgnoringUser(targetUser, true);
                message = simpleChat.getConfig().getString("language.ignoring-user");
            }
        }

        user.sendMessage(simpleChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n"));
    }

}

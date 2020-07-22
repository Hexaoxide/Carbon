package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
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

        if (user.isIgnoringUser(targetUser)) {
            user.setIgnoringUser(targetUser, false);
            user.sendMessage(processMessage(player, simpleChat.getConfig().getString("language.not-ignoring-user")));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(simpleChat, () -> {
                Permission permission = simpleChat.getPermission();

                if (permission.playerHas(null, targetUser.asOfflinePlayer(), "simplechat.ignore.exempt")) {
                    user.sendMessage(processMessage(player, simpleChat.getConfig().getString("language.ignore-exempt")));
                } else {
                    user.setIgnoringUser(targetUser, true);
                    user.sendMessage(processMessage(player, simpleChat.getConfig().getString("language.ignoring-user")));
                }
            });

        }
    }

    private Component processMessage(Player player, String message) {
        return simpleChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n");
    }

}

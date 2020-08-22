package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("ignore")
@CommandPermission("carbonchat.ignore")
public class IgnoreCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void baseCommand(Player player, ChatUser targetUser) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        if (user.isIgnoringUser(targetUser)) {
            user.setIgnoringUser(targetUser, false);
            user.sendMessage(processMessage(player, carbonChat.getLanguage().getString("not-ignoring-user")));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                Permission permission = carbonChat.getPermission();

                if (permission.playerHas(null, targetUser.asOfflinePlayer(), "carbonchat.ignore.exempt")) {
                    user.sendMessage(processMessage(player, carbonChat.getLanguage().getString("ignore-exempt")));
                } else {
                    user.setIgnoringUser(targetUser, true);
                    user.sendMessage(processMessage(player, carbonChat.getLanguage().getString("ignoring-user")));
                }
            });

        }
    }

    private Component processMessage(Player player, String message) {
        return carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n");
    }

}

package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
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
    public void baseCommand(Player player, ChatUser targetUser) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        if (user.isIgnoringUser(targetUser)) {
            user.setIgnoringUser(targetUser, false);
            user.sendMessage(processMessage(player, carbonChat.getConfig().getString("language.not-ignoring-user")));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                Permission permission = carbonChat.getPermission();

                if (permission.playerHas(null, targetUser.asOfflinePlayer(), "carbonchat.ignore.exempt")) {
                    user.sendMessage(processMessage(player, carbonChat.getConfig().getString("language.ignore-exempt")));
                } else {
                    user.setIgnoringUser(targetUser, true);
                    user.sendMessage(processMessage(player, carbonChat.getConfig().getString("language.ignoring-user")));
                }
            });

        }
    }

    private Component processMessage(Player player, String message) {
        return carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n");
    }

}

package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.entity.Player;

@CommandAlias("nick|nickname")
@CommandPermission("carbonchat.nickname")
public class NicknameCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    public void baseCommand(Player player, String nickname, @Optional ChatUser target) {
        ChatUser sender = carbonChat.getUserService().wrap(player);

        // Reset nickname
        if (nickname.equalsIgnoreCase("off")) {
            nickname = null;
        }

        // Reset other player's nickname
        if (target != null && nickname != null && nickname.equals(target.asOfflinePlayer().getName())) {
            nickname = null;
        }

        // Reset own nickname
        if (target == null && nickname != null && nickname.equals(sender.asOfflinePlayer().getName())) {
            nickname = null;
        }

        if (target != null) {
            target.setNickname(nickname);

            String message;

            if (nickname == null) {
                message = carbonChat.getConfig().getString("language.other-nickname-reset");
            } else {
                message = carbonChat.getConfig().getString("language.other-nickname-set");
            }

            sender.sendMessage(carbonChat.getAdventureManager().processMessage(
                    message, "nickname", nickname == null ? "" : nickname,
                    "user", target.asOfflinePlayer().getName()));
        } else {
            sender.setNickname(nickname);

            String message;

            if (nickname == null) {
                message = carbonChat.getConfig().getString("language.nickname-reset");
            } else {
                message = carbonChat.getConfig().getString("language.nickname-set");
            }

            sender.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player,
                    message, "nickname", nickname == null ? "" : nickname));
        }
    }

}

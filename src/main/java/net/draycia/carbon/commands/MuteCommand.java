package net.draycia.carbon.commands;

import co.aikar.commands.annotation.*;
import co.aikar.commands.BaseCommand;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("mute")
@CommandPermission("carbonchat.mute")
public class MuteCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void baseCommand(CommandSender sender, ChatUser targetUser) {
        Audience audience = carbonChat.getAdventureManager().getAudiences().audience(sender);

        if (targetUser.isMuted()) {
            targetUser.setMuted(false);
            String format = carbonChat.getLanguage().getString("no-longer-muted");

            Component message = carbonChat.getAdventureManager().processMessage(format,  "br", "\n",
                    "player", targetUser.asOfflinePlayer().getName());

            audience.sendMessage(message);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                Permission permission = carbonChat.getPermission();
                String format;

                if (permission.playerHas(null, targetUser.asOfflinePlayer(), "carbonchat.mute.exempt")) {
                    format = carbonChat.getLanguage().getString("mute-exempt");
                } else {
                    targetUser.setMuted(true);
                    format = carbonChat.getLanguage().getString("is-now-muted");
                }

                Component message = carbonChat.getAdventureManager().processMessage(format,  "br", "\n",
                        "player", targetUser.asOfflinePlayer().getName());

                audience.sendMessage(message);
            });
        }
    }

}

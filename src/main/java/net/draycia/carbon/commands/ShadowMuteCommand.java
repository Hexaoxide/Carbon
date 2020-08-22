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

@CommandAlias("shadowmute|sm|smute")
@CommandPermission("carbonchat.shadowmute")
public class ShadowMuteCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void baseCommand(CommandSender sender, ChatUser targetUser) {
        Audience audience = carbonChat.getAdventureManager().getAudiences().audience(sender);

        if (targetUser.isShadowMuted()) {
            targetUser.setShadowMuted(false);
            String format = carbonChat.getLanguage().getString("no-longer-shadow-muted");

            Component message = carbonChat.getAdventureManager().processMessage(format, "br", "\n",
                    "player", targetUser.asOfflinePlayer().getName());

            audience.sendMessage(message);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                Permission permission = carbonChat.getPermission();
                String format;

                if (permission.playerHas(null, targetUser.asOfflinePlayer(), "carbonchat.shadowmute.exempt")) {
                    format = carbonChat.getLanguage().getString("shadow-mute-exempt");
                } else {
                    targetUser.setShadowMuted(true);
                    format = carbonChat.getLanguage().getString("is-now-shadow-muted");
                }

                Component message = carbonChat.getAdventureManager().processMessage(format, "br", "\n",
                        "player", targetUser.asOfflinePlayer().getName());

                audience.sendMessage(message);
            });
        }
    }
}

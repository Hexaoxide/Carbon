package net.draycia.simplechatmoderation.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechatmoderation.SimpleChatModeration;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("mute")
@CommandPermission("simplechat.mute")
public class MuteCommand extends BaseCommand {

    private SimpleChatModeration moderation;

    public MuteCommand(SimpleChatModeration moderation) {
        this.moderation = moderation;
    }

    @Default
    @CommandCompletion("@players")
    public void baseCommand(CommandSender sender, ChatUser targetUser) {
        Audience audience = moderation.getSimpleChat().getAdventureManager().getAudiences().audience(sender);

        if (targetUser.isMuted()) {
            targetUser.setMuted(false);
            String format = moderation.getConfig().getString("language.no-longer-muted");

            Component message = moderation.getSimpleChat().getAdventureManager().processMessage(format,  "br", "\n",
                    "player", targetUser.asOfflinePlayer().getName());

            audience.sendMessage(message);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(moderation, () -> {
                Permission permission = moderation.getSimpleChat().getPermission();
                String format;

                if (permission.playerHas(null, targetUser.asOfflinePlayer(), "simplechat.mute.exempt")) {
                    format = moderation.getConfig().getString("language.mute-exempt");
                } else {
                    targetUser.setMuted(true);
                    format = moderation.getConfig().getString("language.is-now-muted");
                }

                Component message = moderation.getSimpleChat().getAdventureManager().processMessage(format,  "br", "\n",
                        "player", targetUser.asOfflinePlayer().getName());

                audience.sendMessage(message);
            });
        }
    }

}

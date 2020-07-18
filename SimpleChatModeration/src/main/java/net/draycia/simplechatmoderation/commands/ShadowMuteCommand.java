package net.draycia.simplechatmoderation.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechatmoderation.SimpleChatModeration;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;

@CommandAlias("shadowmute|sm|smute")
@CommandPermission("simplechat.shadowmute")
public class ShadowMuteCommand extends BaseCommand {

    private SimpleChatModeration moderation;

    public ShadowMuteCommand(SimpleChatModeration moderation) {
        this.moderation = moderation;
    }

    @Default
    @CommandCompletion("@players")
    public void baseCommand(CommandSender sender, ChatUser targetUser) {
        String format;

        if (targetUser.isShadowMuted()) {
            targetUser.setShadowMuted(false);
            format = moderation.getConfig().getString("language.no-longer-shadow-muted");
        } else {
            Permission permission = moderation.getSimpleChat().getPermission();

            if (permission.playerHas(null, targetUser.asOfflinePlayer(), "simplechat.shadowmute.exempt")) {
                format = moderation.getConfig().getString("language.shadow-mute-exempt");
            } else {
                targetUser.setShadowMuted(true);
                format = moderation.getConfig().getString("language.is-now-shadow-muted");
            }
        }

        Component message = moderation.getSimpleChat().getAdventureManager().processMessage(format, "br", "\n",
                "player", targetUser.asOfflinePlayer().getName());

        moderation.getSimpleChat().getAdventureManager().getAudiences().audience(sender).sendMessage(message);
    }

}

package net.draycia.carbonmoderation.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbonmoderation.CarbonChatModeration;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("shadowmute|sm|smute")
@CommandPermission("simplechat.shadowmute")
public class ShadowMuteCommand extends BaseCommand {

    private CarbonChatModeration moderation;

    public ShadowMuteCommand(CarbonChatModeration moderation) {
        this.moderation = moderation;
    }

    @Default
    @CommandCompletion("@players")
    public void baseCommand(CommandSender sender, ChatUser targetUser) {
        Audience audience = moderation.getCarbonChat().getAdventureManager().getAudiences().audience(sender);

        if (targetUser.isShadowMuted()) {
            targetUser.setShadowMuted(false);
            String format = moderation.getConfig().getString("language.no-longer-shadow-muted");

            Component message = moderation.getCarbonChat().getAdventureManager().processMessage(format, "br", "\n",
                    "player", targetUser.asOfflinePlayer().getName());

            audience.sendMessage(message);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(moderation, () -> {
                Permission permission = moderation.getCarbonChat().getPermission();
                String format;

                if (permission.playerHas(null, targetUser.asOfflinePlayer(), "simplechat.shadowmute.exempt")) {
                    format = moderation.getConfig().getString("language.shadow-mute-exempt");
                } else {
                    targetUser.setShadowMuted(true);
                    format = moderation.getConfig().getString("language.is-now-shadow-muted");
                }

                Component message = moderation.getCarbonChat().getAdventureManager().processMessage(format, "br", "\n",
                        "player", targetUser.asOfflinePlayer().getName());

                audience.sendMessage(message);
            });
        }
    }
}

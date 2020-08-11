package net.draycia.carbonmoderation.commands;

import net.draycia.carbon.libs.co.aikar.commands.BaseCommand;
import net.draycia.carbon.libs.co.aikar.commands.annotation.CommandAlias;
import net.draycia.carbon.libs.co.aikar.commands.annotation.CommandCompletion;
import net.draycia.carbon.libs.co.aikar.commands.annotation.CommandPermission;
import net.draycia.carbon.libs.co.aikar.commands.annotation.Default;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbonmoderation.CarbonChatModeration;
import net.draycia.carbon.libs.net.kyori.adventure.audience.Audience;
import net.draycia.carbon.libs.net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("mute")
@CommandPermission("carbonchat.mute")
public class MuteCommand extends BaseCommand {

    private final CarbonChatModeration moderation;

    public MuteCommand(CarbonChatModeration moderation) {
        this.moderation = moderation;
    }

    @Default
    @CommandCompletion("@players")
    public void baseCommand(CommandSender sender, ChatUser targetUser) {
        Audience audience = moderation.getCarbonChat().getAdventureManager().getAudiences().audience(sender);

        if (targetUser.isMuted()) {
            targetUser.setMuted(false);
            String format = moderation.getConfig().getString("language.no-longer-muted");

            Component message = moderation.getCarbonChat().getAdventureManager().processMessage(format,  "br", "\n",
                    "player", targetUser.asOfflinePlayer().getName());

            audience.sendMessage(message);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(moderation, () -> {
                Permission permission = moderation.getCarbonChat().getPermission();
                String format;

                if (permission.playerHas(null, targetUser.asOfflinePlayer(), "carbonchat.mute.exempt")) {
                    format = moderation.getConfig().getString("language.mute-exempt");
                } else {
                    targetUser.setMuted(true);
                    format = moderation.getConfig().getString("language.is-now-muted");
                }

                Component message = moderation.getCarbonChat().getAdventureManager().processMessage(format,  "br", "\n",
                        "player", targetUser.asOfflinePlayer().getName());

                audience.sendMessage(message);
            });
        }
    }

}

package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@CommandAlias("shadowmute|sm|smute")
@CommandPermission("simplechat.shadowmute")
public class ShadowMuteCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public ShadowMuteCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    public void baseCommand(CommandSender sender, OfflinePlayer target) {
        boolean isNowMuted = simpleChat.toggleShadowMute(target);

        String format;

        if (isNowMuted) {
            format = simpleChat.getConfig().getString("language.is-now-shadow-muted");
        } else {
            format = simpleChat.getConfig().getString("language.no-longer-shadow-muted");
        }

        Component message = MiniMessageParser.parseFormat(format, "user", target.getName());

        simpleChat.getAudiences().audience(sender).sendMessage(message);
    }

}

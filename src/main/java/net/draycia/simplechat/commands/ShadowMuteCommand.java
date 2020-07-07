package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

@CommandAlias("shadowmute|sm|smute")
@CommandPermission("simplechat.shadowmute")
public class ShadowMuteCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public ShadowMuteCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    @CommandCompletion("@players")
    public void baseCommand(CommandSender sender, ChatUser targetUser) {
        String format;

        if (targetUser.isShadowMuted()) {
            targetUser.setShadowMuted(false);
            format = simpleChat.getConfig().getString("language.no-longer-shadow-muted");
        } else {
            targetUser.setShadowMuted(true);
            format = simpleChat.getConfig().getString("language.is-now-shadow-muted");
        }

        Component message = MiniMessage.get().parse(format, "user", targetUser.asOfflinePlayer().getName());

        simpleChat.getAudiences().audience(sender).sendMessage(message);
    }

}

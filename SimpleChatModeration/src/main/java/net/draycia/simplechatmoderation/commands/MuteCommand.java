package net.draycia.simplechatmoderation.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechatmoderation.SimpleChatModeration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
        String format;

        if (targetUser.isMuted()) {
            targetUser.setMuted(true);
            format = moderation.getConfig().getString("language.no-longer-muted");
        } else {
            targetUser.setMuted(false);
            format = moderation.getConfig().getString("language.is-now-muted");
        }

        Component message = MiniMessage.get().parse(format,  "br", "\n",
                "user", targetUser.asOfflinePlayer().getName());

        moderation.getSimpleChat().getAudiences().audience(sender).sendMessage(message);
    }

}

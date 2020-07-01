package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("msg|whisper|message|w")
@CommandPermission("simplechat.message")
public class MessageCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public MessageCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    public void baseCommand(Player player, String target, String... args) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);

        if (simpleChat.playerHasPlayerIgnored(targetPlayer, player)) {
            return;
        }

        String message = String.join(" ", args);

        String toPlayerFormat = simpleChat.getConfig().getString("language.message-to-other");
        String fromPlayerFormat = simpleChat.getConfig().getString("language.message-from-other");

        Component toPlayerComponent = MiniMessage.instance().parse(toPlayerFormat, "message", message,
                "target", targetPlayer.getName());

        Component fromPlayerComponent = MiniMessage.instance().parse(fromPlayerFormat, "message", message,
                "sender", player.getName());

        simpleChat.getAudiences().player(player).sendMessage(toPlayerComponent);

        if (targetPlayer.isOnline()) {
            simpleChat.getAudiences().player(targetPlayer.getPlayer()).sendMessage(fromPlayerComponent);
        } else {
            // TODO: cross server msg support, don't forget to include /ignore support
        }
    }

}

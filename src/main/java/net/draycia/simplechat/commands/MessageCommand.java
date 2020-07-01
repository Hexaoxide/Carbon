package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
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
    @CommandCompletion("@players")
    public void baseCommand(Player player, String target, String... args) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);

        if (targetPlayer == null) {
            return;
        }

        simpleChat.sendPlayerPrivateMessage(player, targetPlayer, String.join(" ", args));
    }

}

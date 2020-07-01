package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("ignore")
@CommandPermission("simplechat.ignore")
public class IgnoreCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public IgnoreCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    @CommandCompletion("@players")
    public void baseCommand(Player player, OfflinePlayer target) {
        String message;

        if (simpleChat.togglePlayerIgnoringPlayer(player, target)) {
            message = simpleChat.getConfig().getString("language.ignoring-user");
        } else {
            message = simpleChat.getConfig().getString("language.not-ignoring-user");
        }

        simpleChat.getAudiences().player(player).sendMessage(MiniMessage.instance().parse(message));
    }

}

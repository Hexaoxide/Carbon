package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.storage.UserChannelSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("tch|togglec|togglechannel|toggle")
@CommandPermission("simplechat.toggle")
public class ToggleCommand extends BaseCommand {

    @Dependency
    private SimpleChat simpleChat;

    @Default
    @CommandCompletion("@chatchannel @players")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel) {
        ChatUser user = simpleChat.getUserService().wrap(player);

        String message;

        UserChannelSettings settings = user.getChannelSettings(channel);

        if (settings.isIgnored()) {
            settings.setIgnoring(false);
            message = channel.getToggleOffMessage();
        } else {
            settings.setIgnoring(true);
            message = channel.getToggleOnMessage();
        }

        user.sendMessage(simpleChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n",
                "color", "<color:" + channel.getColor().toString() + ">", "channel", channel.getName()));
    }

    @CommandPermission("simplechat.toggle.others")
    @Subcommand("other")
    @CommandCompletion("@chatchannel @players")
    public void baseCommand(CommandSender sender, @Conditions("canuse:true") ChatChannel channel, ChatUser user) {
        String message;
        String otherMessage;

        UserChannelSettings settings = user.getChannelSettings(channel);

        if (settings.isIgnored()) {
            settings.setIgnoring(false);
            message = channel.getToggleOffMessage();
            otherMessage = channel.getToggleOtherOffMessage();
        } else {
            settings.setIgnoring(true);
            message = channel.getToggleOnMessage();
            otherMessage = channel.getToggleOtherOnMessage();
        }

        user.sendMessage(simpleChat.getAdventureManager().processMessage(message, "br", "\n",
                "color", "<color:" + channel.getColor().toString() + ">", "channel", channel.getName()));

        simpleChat.getAdventureManager().getAudiences().audience(sender).sendMessage(simpleChat.getAdventureManager().processMessage(otherMessage,
                "br", "\n", "color", "<color:" + channel.getColor().toString() + ">",
                "channel", channel.getName(), "player", user.asOfflinePlayer().getName()));
    }
}

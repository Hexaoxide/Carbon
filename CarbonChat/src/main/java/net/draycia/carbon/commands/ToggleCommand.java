package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserChannelSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("tch|togglec|togglechannel|toggle")
@CommandPermission("carbonchat.toggle")
public class ToggleCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @CommandCompletion("@chatchannel @players")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        String message;

        UserChannelSettings settings = user.getChannelSettings(channel);

        if (settings.isIgnored()) {
            settings.setIgnoring(false);
            message = channel.getToggleOffMessage();
        } else {
            settings.setIgnoring(true);
            message = channel.getToggleOnMessage();
        }

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n",
                "color", "<color:" + channel.getColor().toString() + ">", "channel", channel.getName()));
    }

    @CommandPermission("carbonchat.toggle.others")
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

        user.sendMessage(carbonChat.getAdventureManager().processMessage(message, "br", "\n",
                "color", "<color:" + channel.getColor().toString() + ">", "channel", channel.getName()));

        carbonChat.getAdventureManager().getAudiences().audience(sender).sendMessage(carbonChat.getAdventureManager().processMessage(otherMessage,
                "br", "\n", "color", "<color:" + channel.getColor().toString() + ">",
                "channel", channel.getName(), "player", user.asOfflinePlayer().getName()));
    }
}

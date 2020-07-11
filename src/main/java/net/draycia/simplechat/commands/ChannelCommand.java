package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.storage.UserChannelSettings;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("ch|channel|switch")
@CommandPermission("simplechat.switch")
public class ChannelCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public ChannelCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    @CommandCompletion("@chatchannel")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel, @Optional String[] args) {
        ChatUser user = simpleChat.getUserService().wrap(player);

        if (args == null || args.length == 0) {
            user.setSelectedChannel(channel);

            user.sendMessage(MiniMessage.get().parse(channel.getSwitchMessage(),
                    "br", "\n",
                    "color", "<" + channel.getColor().toString() + ">",
                    "channel", channel.getName()));
        } else {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
                channel.sendMessage(user, String.join(" ", args), false);
            });
        }
    }

    @CommandPermission("simplechat.switch.others")
    @Subcommand("other")
    @CommandCompletion("@chatchannel @players")
    public void baseCommand(Player player, ChatChannel channel, ChatUser user) {
        user.setSelectedChannel(channel);

        String message = channel.getSwitchMessage();
        String otherMessage = channel.getSwitchOtherMessage();

        user.sendMessage(MiniMessage.get().parse(message, "br", "\n",
                "color", "<color:" + channel.getColor().toString() + ">", "channel", channel.getName()));

        simpleChat.getUserService().wrap(player).sendMessage(MiniMessage.get().parse(otherMessage, "br", "\n",
                "color", "<color:" + channel.getColor().toString() + ">", "channel", channel.getName(),
                "player", user.asOfflinePlayer().getName()));
    }

}

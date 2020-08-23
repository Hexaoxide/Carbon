package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("ch|channel|switch")
@CommandPermission("carbonchat.switch")
public class ChannelCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @CommandCompletion("@chatchannel")
    @Syntax("<channel> [message]")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel, @Optional String[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        if (args == null || args.length == 0) {
            user.setSelectedChannel(channel);

            user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, channel.getSwitchMessage(),
                    "br", "\n",
                    "color", "<" + channel.getChannelColor(user).toString() + ">",
                    "channel", channel.getName()));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                Component component = channel.sendMessage(user, String.join(" ", args), false);

                carbonChat.getLogger().info(LegacyComponentSerializer.legacySection().serialize(component)
                        .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
            });
        }
    }

    @CommandPermission("carbonchat.switch.others")
    @Subcommand("other")
    @CommandCompletion("@chatchannel @players")
    @Syntax("<channel> <user>")
    public void baseCommand(CommandSender sender, @Conditions("exists:true") ChatChannel channel, ChatUser user) {
        user.setSelectedChannel(channel);

        String message = channel.getSwitchMessage();
        String otherMessage = channel.getSwitchOtherMessage();

        user.sendMessage(carbonChat.getAdventureManager().processMessage(message, "br", "\n",
                "color", "<color:" + channel.getChannelColor(user).toString() + ">", "channel", channel.getName()));

        carbonChat.getAdventureManager().getAudiences().audience(sender).sendMessage(carbonChat.getAdventureManager().processMessage(otherMessage, "br", "\n",
                "color", "<color:" + channel.getChannelColor(user).toString() + ">", "channel", channel.getName(),
                "player", user.asOfflinePlayer().getName()));
    }

}

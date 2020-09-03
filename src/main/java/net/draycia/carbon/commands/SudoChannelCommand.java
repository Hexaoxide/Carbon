package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.List;

public class SudoChannelCommand {

    private final CarbonChat carbonChat;

    public SudoChannelCommand(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        if (!carbonChat.getConfig().getBoolean("commands.sudochannel.enabled")) {
            return;
        }

        String commandName = carbonChat.getConfig().getString("commands.sudochannel.name", "sudochannel");
        List<String> commandAliases = carbonChat.getConfig().getStringList("commands.sudochannel.aliases");

        LinkedHashMap<String, Argument> setOtherChannelArguments = new LinkedHashMap<>();
        setOtherChannelArguments.put("player", CarbonUtils.onlineChatUserArgument());
        setOtherChannelArguments.put("channel", CarbonUtils.channelArgument());

        new CommandAPICommand(commandName)
                .withArguments(setOtherChannelArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.channel.others"))
                .executes((sender, args) -> { this.setOtherChannel(sender, (ChatUser) args[0], (ChatChannel) args[1]); })
                .register();

        LinkedHashMap<String, Argument> sendMessageOtherArguments = new LinkedHashMap<>();
        sendMessageOtherArguments.put("player", CarbonUtils.onlineChatUserArgument());
        sendMessageOtherArguments.put("channel", CarbonUtils.channelArgument());
        sendMessageOtherArguments.put("message", new GreedyStringArgument());

        new CommandAPICommand(commandName)
                .withArguments(sendMessageOtherArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.channel.others.message"))
                .executes((sender, args) -> { this.sendMessageOther(sender, (ChatUser) args[0], (ChatChannel) args[1], (String) args[2]); })
                .register();
    }

    private void setOtherChannel(CommandSender sender, ChatUser user, ChatChannel channel) {
        user.setSelectedChannel(channel);

        String message = channel.getSwitchMessage();
        String otherMessage = channel.getSwitchOtherMessage();

        user.sendMessage(carbonChat.getAdventureManager().processMessage(message, "br", "\n",
                "color", "<color:" + channel.getChannelColor(user).toString() + ">", "channel", channel.getName()));

        carbonChat.getAdventureManager().getAudiences().audience(sender).sendMessage(carbonChat.getAdventureManager().processMessage(otherMessage, "br", "\n",
                "color", "<color:" + channel.getChannelColor(user).toString() + ">", "channel", channel.getName(),
                "player", user.asOfflinePlayer().getName()));
    }

    private void sendMessageOther(CommandSender sender, ChatUser user, ChatChannel channel, String message) {
        Component component = channel.sendMessage(user, message, false);

        carbonChat.getLogger().info(CarbonChat.LEGACY.serialize(component)
                .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
    }

}

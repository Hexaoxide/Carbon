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
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;

public class ChannelCommand {

    private final CarbonChat carbonChat;

    public ChannelCommand(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        String commandName = carbonChat.getConfig().getString("commands.channel.name", "channel");
        List<String> commandAliases = carbonChat.getConfig().getStringList("commands.channel.aliases");

        LinkedHashMap<String, Argument> setChannelArguments = new LinkedHashMap<>();
        setChannelArguments.put("channel", CarbonUtils.channelArgument());

        new CommandAPICommand(commandName)
                .withArguments(setChannelArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.channel"))
                .executesPlayer(this::setChannel)
                .register();

        LinkedHashMap<String, Argument> sendMessageArguments = new LinkedHashMap<>();
        sendMessageArguments.put("channel", CarbonUtils.channelArgument());
        sendMessageArguments.put("message", new GreedyStringArgument());

        new CommandAPICommand(commandName)
                .withArguments(sendMessageArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.channel.message"))
                .executesPlayer(this::sendMessage)
                .register();
    }

    private void setChannel(Player player, Object[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);
        ChatChannel channel = (ChatChannel) args[0];

        if (user.getChannelSettings(channel).isIgnored()) {
            user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, channel.getCannotUseMessage(),
                    "br", "\n",
                    "color", "<" + channel.getChannelColor(user).toString() + ">",
                    "channel", channel.getName()));

            return;
        }

        user.setSelectedChannel(channel);

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, channel.getSwitchMessage(),
                "br", "\n",
                "color", "<" + channel.getChannelColor(user).toString() + ">",
                "channel", channel.getName()));
    }

    private void sendMessage(Player player, Object[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);
        ChatChannel channel = (ChatChannel) args[0];
        String message = (String) args[1];

        Component component = channel.sendMessage(user, message, false);

        carbonChat.getLogger().info(CarbonChat.LEGACY.serialize(component)
                .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
    }

}

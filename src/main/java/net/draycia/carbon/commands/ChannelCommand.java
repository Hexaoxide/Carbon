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
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;

public class ChannelCommand {

    private final CarbonChat carbonChat;

    public ChannelCommand(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        String commandName = carbonChat.getConfig().getString("commands.switch.name", "switch");
        List<String> commandAliases = carbonChat.getConfig().getStringList("commands.switch.aliases");

        LinkedHashMap<String, Argument> setChannelArguments = new LinkedHashMap<>();
        setChannelArguments.put("channel", CarbonUtils.channelArgument());

        new CommandAPICommand(commandName)
                .withArguments(setChannelArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.switch"))
                .executesPlayer(this::setChannel)
                .register();

        LinkedHashMap<String, Argument> setOtherChannelArguments = new LinkedHashMap<>();
        setOtherChannelArguments.put("channel", CarbonUtils.channelArgument());
        setOtherChannelArguments.put("player", CarbonUtils.chatUserArgument());

        new CommandAPICommand(commandName)
                .withArguments(setOtherChannelArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.switch.other"))
                .executes(this::setOtherChannel)
                .register();

        LinkedHashMap<String, Argument> sendMessageArguments = new LinkedHashMap<>();
        sendMessageArguments.put("channel", CarbonUtils.channelArgument());
        sendMessageArguments.put("message", new GreedyStringArgument());

        new CommandAPICommand(commandName)
                .withArguments(sendMessageArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.switch.message"))
                .executesPlayer(this::sendMessage)
                .register();

        LinkedHashMap<String, Argument> sendMessageOtherArguments = new LinkedHashMap<>();
        sendMessageOtherArguments.put("player", CarbonUtils.chatUserArgument());
        sendMessageOtherArguments.put("channel", CarbonUtils.channelArgument());
        sendMessageOtherArguments.put("message", new GreedyStringArgument());

        new CommandAPICommand(commandName)
                .withArguments(sendMessageOtherArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.switch.other.message"))
                .executes(this::sendMessageOther)
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

    private void setOtherChannel(CommandSender sender, Object[] args) {
        // @CommandPermission("carbonchat.switch.others")
        ChatUser user = (ChatUser) args[0];
        ChatChannel channel = (ChatChannel) args[1];

        user.setSelectedChannel(channel);

        String message = channel.getSwitchMessage();
        String otherMessage = channel.getSwitchOtherMessage();

        user.sendMessage(carbonChat.getAdventureManager().processMessage(message, "br", "\n",
                "color", "<color:" + channel.getChannelColor(user).toString() + ">", "channel", channel.getName()));

        carbonChat.getAdventureManager().getAudiences().audience(sender).sendMessage(carbonChat.getAdventureManager().processMessage(otherMessage, "br", "\n",
                "color", "<color:" + channel.getChannelColor(user).toString() + ">", "channel", channel.getName(),
                "player", user.asOfflinePlayer().getName()));
    }

    private void sendMessage(Player player, Object[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);
        ChatChannel channel = (ChatChannel) args[0];
        String message = (String) args[1];

        Component component = channel.sendMessage(user, message, false);

        carbonChat.getLogger().info(CarbonChat.LEGACY.serialize(component)
                .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
    }

    private void sendMessageOther(CommandSender sender, Object[] args) {
        ChatUser user = (ChatUser) args[0];
        ChatChannel channel = (ChatChannel) args[1];
        String message = (String) args[2];

        Component component = channel.sendMessage(user, message, false);

        carbonChat.getLogger().info(CarbonChat.LEGACY.serialize(component)
                .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
    }

}

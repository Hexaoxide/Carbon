package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class ChannelCommand {

    private final CarbonChat carbonChat;

    public ChannelCommand(CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
        this.carbonChat = carbonChat;

        if (!commandSettings.isEnabled()) {
            return;
        }

        CommandUtils.handleDuplicateCommands(commandSettings);

        LinkedHashMap<String, Argument> setChannelArguments = new LinkedHashMap<>();
        setChannelArguments.put("channel", CarbonUtils.channelArgument());

        new CommandAPICommand(commandSettings.getName())
                .withArguments(setChannelArguments)
                .withAliases(commandSettings.getAliasesArray())
                .withPermission(CommandPermission.fromString("carbonchat.channel"))
                .executesPlayer(this::setChannel)
                .register();

        LinkedHashMap<String, Argument> sendMessageArguments = new LinkedHashMap<>();
        sendMessageArguments.put("channel", CarbonUtils.channelArgument());
        sendMessageArguments.put("message", new GreedyStringArgument());

        new CommandAPICommand(commandSettings.getName())
                .withArguments(sendMessageArguments)
                .withAliases(commandSettings.getAliasesArray())
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

        carbonChat.getAdventureManager().getAudiences().console().sendMessage(component);
    }

}

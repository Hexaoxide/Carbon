package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;

public class AliasedChannelCommand {

    private final CarbonChat carbonChat;
    private final ChatChannel chatChannel;
    private final String commandName;

    public AliasedChannelCommand(CarbonChat carbonChat, ChatChannel chatChannel) {
        this.carbonChat = carbonChat;
        this.chatChannel = chatChannel;

        this.commandName = chatChannel.getKey();

        LinkedHashMap<String, Argument> setChannelArguments = new LinkedHashMap<>();

        new CommandAPICommand(commandName)
                .withArguments(setChannelArguments)
                .withPermission(CommandPermission.fromString("carbonchat.channel"))
                .executesPlayer(this::setChannel)
                .register();

        LinkedHashMap<String, Argument> sendMessageArguments = new LinkedHashMap<>();
        sendMessageArguments.put("message", new GreedyStringArgument());

        new CommandAPICommand(commandName)
                .withArguments(sendMessageArguments)
                .withPermission(CommandPermission.fromString("carbonchat.channel.message"))
                .executesPlayer(this::sendMessage)
                .register();
    }

    private void setChannel(Player player, Object[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        if (user.getChannelSettings(getChatChannel()).isIgnored()) {
            user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, getChatChannel().getCannotUseMessage(),
                    "br", "\n",
                    "color", "<" + getChatChannel().getChannelColor(user).toString() + ">",
                    "channel", getChatChannel().getName()));

            return;
        }

        user.setSelectedChannel(getChatChannel());

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, getChatChannel().getSwitchMessage(),
                "br", "\n",
                "color", "<" + getChatChannel().getChannelColor(user).toString() + ">",
                "channel", getChatChannel().getName()));
    }

    private void sendMessage(Player player, Object[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);
        String message = (String) args[0];

        Component component = getChatChannel().sendMessage(user, message, false);

        carbonChat.getLogger().info(CarbonChat.LEGACY.serialize(component)
                .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
    }

    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    public String getCommandName() {
        return commandName;
    }
}

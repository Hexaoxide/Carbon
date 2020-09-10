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
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class AliasedChannelCommand {

    @NonNull
    private final CarbonChat carbonChat;

    @NonNull
    private final ChatChannel chatChannel;

    @NonNull
    private final String commandName;

    public AliasedChannelCommand(@NonNull CarbonChat carbonChat, @NonNull ChatChannel chatChannel) {
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

    private void setChannel(@NonNull Player player, @NonNull Object @NonNull [] args) {
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

    private void sendMessage(@NonNull Player player, @NonNull Object @NonNull [] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);
        String message = (String) args[0];

        Component component = getChatChannel().sendMessage(user, message, false);

        carbonChat.getAdventureManager().getAudiences().console().sendMessage(component);
    }

    @NonNull
    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    @NonNull
    public String getCommandName() {
        return commandName;
    }
}

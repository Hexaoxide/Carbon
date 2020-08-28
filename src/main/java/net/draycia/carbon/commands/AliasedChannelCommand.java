package net.draycia.carbon.commands;

import co.aikar.commands.annotation.*;
import dev.jorel.commandapi.CommandAPICommand;
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

@CommandAlias("%channelName")
@CommandPermission("carbonchat.switch")
public class AliasedChannelCommand {

    private final CarbonChat carbonChat;
    private final ChatChannel chatChannel;
    private final String commandName;

    public AliasedChannelCommand(CarbonChat carbonChat, ChatChannel chatChannel) {
        this.carbonChat = carbonChat;
        this.chatChannel = chatChannel;

        this.commandName = carbonChat.getConfig().getString("commands.switch.name", "switch");
        List<String> commandAliases = carbonChat.getConfig().getStringList("commands.switch.aliases");

        LinkedHashMap<String, Argument> setChannelArguments = new LinkedHashMap<>();

        new CommandAPICommand(commandName)
                .withArguments(setChannelArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(dev.jorel.commandapi.CommandPermission.fromString("carbonchat.switch"))
                .executesPlayer(this::setChannel)
                .register();

        LinkedHashMap<String, Argument> setOtherChannelArguments = new LinkedHashMap<>();
        setOtherChannelArguments.put("player", CarbonUtils.chatUserArgument());

        new CommandAPICommand(commandName)
                .withArguments(setOtherChannelArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(dev.jorel.commandapi.CommandPermission.fromString("carbonchat.switch.other"))
                .executes(this::setOtherChannel)
                .register();

        LinkedHashMap<String, Argument> sendMessageArguments = new LinkedHashMap<>();
        sendMessageArguments.put("message", new GreedyStringArgument());

        new CommandAPICommand(commandName)
                .withArguments(sendMessageArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(dev.jorel.commandapi.CommandPermission.fromString("carbonchat.switch.message"))
                .executesPlayer(this::sendMessage)
                .register();

        LinkedHashMap<String, Argument> sendMessageOtherArguments = new LinkedHashMap<>();
        sendMessageOtherArguments.put("player", CarbonUtils.chatUserArgument());
        sendMessageOtherArguments.put("message", new GreedyStringArgument());

        new CommandAPICommand(commandName)
                .withArguments(sendMessageOtherArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(dev.jorel.commandapi.CommandPermission.fromString("carbonchat.switch.other.message"))
                .executes(this::sendMessageOther)
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

    private void setOtherChannel(CommandSender sender, Object[] args) {
        // @CommandPermission("carbonchat.switch.others")
        ChatUser user = (ChatUser) args[0];

        user.setSelectedChannel(getChatChannel());

        String message = getChatChannel().getSwitchMessage();
        String otherMessage = getChatChannel().getSwitchOtherMessage();

        user.sendMessage(carbonChat.getAdventureManager().processMessage(message, "br", "\n",
                "color", "<color:" + getChatChannel().getChannelColor(user).toString() + ">", "channel", getChatChannel().getName()));

        carbonChat.getAdventureManager().getAudiences().audience(sender).sendMessage(carbonChat.getAdventureManager().processMessage(otherMessage, "br", "\n",
                "color", "<color:" + getChatChannel().getChannelColor(user).toString() + ">", "channel", getChatChannel().getName(),
                "player", user.asOfflinePlayer().getName()));
    }

    private void sendMessage(Player player, Object[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);
        String message = (String) args[1];

        Component component = getChatChannel().sendMessage(user, message, false);

        carbonChat.getLogger().info(CarbonChat.LEGACY.serialize(component)
                .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
    }

    private void sendMessageOther(CommandSender sender, Object[] args) {
        ChatUser user = (ChatUser) args[0];
        String message = (String) args[2];

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

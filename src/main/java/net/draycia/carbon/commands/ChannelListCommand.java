package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ChannelListCommand {

    private final CarbonChat carbonChat;

    public ChannelListCommand(CarbonChat carbonChat, CommandSettings commandSettings) {
        this.carbonChat = carbonChat;

        if (!commandSettings.isEnabled()) {
            return;
        }

        CommandUtils.handleDuplicateCommands(commandSettings);

        LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();

        new CommandAPICommand(commandSettings.getName())
                .withArguments(channelArguments)
                .withAliases(commandSettings.getAliasesArray())
                .withPermission(CommandPermission.fromString("carbonchat.channellist"))
                .executesPlayer(this::executeSelf)
                .register();

        LinkedHashMap<String, Argument> argumentsOther = new LinkedHashMap<>();
        argumentsOther.put("player", CarbonUtils.onlineChatUserArgument());

        new CommandAPICommand(commandSettings.getName())
                .withArguments(argumentsOther)
                .withAliases(commandSettings.getAliasesArray())
                .withPermission(CommandPermission.fromString("carbonchat.channellist.other"))
                .executes(this::executeOther)
                .register();
    }

    public void executeSelf(Player player, Object[] args) {
        Iterator<ChatChannel> allChannels = carbonChat.getChannelManager().getRegistry().values().iterator();
        ChatUser user = carbonChat.getUserService().wrap(player);

        getListAndSend(player, user, allChannels);
    }

    public void executeOther(CommandSender sender, Object[] args) {
        Audience cmdSender = carbonChat.getAdventureManager().getAudiences().audience(sender);
        ChatUser user = (ChatUser) args[0];

        if (!user.isOnline()) {
            String mustBeOnline = carbonChat.getLanguage().getString("user-must-be-online");
            cmdSender.sendMessage(carbonChat.getAdventureManager().processMessage(mustBeOnline, "br", "\n", "player", user.asOfflinePlayer().getName()));
            return;
        }

        Iterator<ChatChannel> allChannels = carbonChat.getChannelManager().getRegistry().values().iterator();
        getListAndSend(sender, user, allChannels);
    }

    private void getListAndSend(CommandSender sender, ChatUser user, Iterator<ChatChannel> allChannels) {
        ChatChannel channel;
        List<ChatChannel> canSee = new ArrayList<>();
        List<ChatChannel> cannotSee = new ArrayList<>();

        while (allChannels.hasNext()) {
            channel = allChannels.next();

            if (channel.canPlayerSee(user, true)) {
                canSee.add(channel);
            } else {
                cannotSee.add(channel);
            }
        }

        Iterator<ChatChannel> visibleChannels = canSee.iterator();
        TextComponent.Builder availableList = TextComponent.builder("");

        makeList(visibleChannels, availableList);

        String availableFormat = carbonChat.getLanguage().getString("available-channels-list");
        Component availableComponent = carbonChat.getAdventureManager().processMessage(availableFormat, "br", "\n");
        availableComponent = availableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), (ac) ->  availableList);

        Audience audience = carbonChat.getAdventureManager().getAudiences().audience(sender);

        audience.sendMessage(availableComponent);

        if (sender.hasPermission("carbonchat.channellist.bypass") && !cannotSee.isEmpty()) {

            Iterator<ChatChannel> invisibleChannels = cannotSee.iterator();
            TextComponent.Builder unavailableList = TextComponent.builder("");

            makeList(invisibleChannels, unavailableList);

            String unavailableFormat = carbonChat.getLanguage().getString("unavailable-channels-list");
            Component unavailableComponent = carbonChat.getAdventureManager().processMessage(unavailableFormat, "br", "\n");
            unavailableComponent = unavailableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), (uac) ->  unavailableList);

            audience.sendMessage(unavailableComponent);
        }
    }

    private void makeList(Iterator<ChatChannel> iterator, TextComponent.Builder list) {
        String listSeparator = carbonChat.getLanguage().getString("channel-list-separator", ", ");
        // TODO: Larry, why did you double assign the listSeparatorComponent?
        Component listSeparatorComponent = TextComponent.of(listSeparator);

        while (iterator.hasNext()) {
            ChatChannel channel = iterator.next();
            list.append(TextComponent.of(channel.getName()));
            if (iterator.hasNext()) {
                    list.append(listSeparatorComponent);
            }
        }
    }
}

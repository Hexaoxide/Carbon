package net.draycia.carbon.managers;

import co.aikar.commands.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.commands.*;
import net.draycia.carbon.storage.ChatUser;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final BukkitCommandManager commandManager;

    public CommandManager(CarbonChat carbonChat) {

        commandManager = new BukkitCommandManager(carbonChat);

        commandManager.enableUnstableAPI("help");

        commandManager.getCommandCompletions().registerCompletion("chatchannel", (context) -> {
            ChatUser user = carbonChat.getUserService().wrap(context.getPlayer());

            List<String> completions = new ArrayList<>();

            for (ChatChannel chatChannel : carbonChat.getChannelManager().getRegistry().values()) {
                if (chatChannel.canPlayerUse(user)) {
                    completions.add(chatChannel.getKey());
                }
            }

            return completions;
        });

        commandManager.getCommandCompletions().registerCompletion("channel", (context) -> {
            List<String> completions = new ArrayList<>();

            for (ChatChannel chatChannel : carbonChat.getChannelManager().getRegistry().values()) {
                    completions.add(chatChannel.getKey());
            }

            return completions;
        });

        commandManager.getCommandContexts().registerContext(ChatChannel.class, (context) -> {
            String name = context.popFirstArg();

            for (ChatChannel chatChannel : carbonChat.getChannelManager().getRegistry().values()) {
                if (chatChannel.getKey().equalsIgnoreCase(name)) {
                    return chatChannel;
                }
            }

            return null;
        });

        commandManager.getCommandContexts().registerContext(ChatUser.class, (context) -> {
            return carbonChat.getUserService().wrap(context.popFirstArg());
        });

        commandManager.getCommandConditions().addCondition(ChatChannel.class,"canuse", (context, execution, value) -> {
            if (value == null) {
                throw new ConditionFailedException(carbonChat.getLanguage().getString("cannot-use-channel"));
            }

            ChatUser user = carbonChat.getUserService().wrap(context.getIssuer().getPlayer());

            if (!value.canPlayerUse(user)) {
                throw new ConditionFailedException(value.getCannotUseMessage());
            }
        });

        commandManager.getCommandConditions().addCondition(ChatChannel.class,"exists", (context, execution, value) -> {
            if (value == null) {
                throw new ConditionFailedException(carbonChat.getLanguage().getString("cannot-use-channel"));
            }
        });

        reloadCommands(carbonChat);
    }

    public void reloadCommands(CarbonChat carbonChat) {
        this.commandManager.registerCommand(new ChannelCommand());
        this.commandManager.registerCommand(new ChannelListCommand());
        new ChatReloadCommand(carbonChat);
        new ClearChatCommand(carbonChat);
        this.commandManager.registerCommand(new IgnoreCommand());
        this.commandManager.registerCommand(new MeCommand());
        this.commandManager.registerCommand(new MessageCommand());
        new MuteCommand(carbonChat);
        new NicknameCommand(carbonChat);
        new ReplyCommand(carbonChat);
        new SetColorCommand(carbonChat);
        new ShadowMuteCommand(carbonChat);
        new SpyChannelCommand(carbonChat);
        new ToggleCommand(carbonChat);
    }

    public BukkitCommandManager getCommandManager() {
        return commandManager;
    }

}

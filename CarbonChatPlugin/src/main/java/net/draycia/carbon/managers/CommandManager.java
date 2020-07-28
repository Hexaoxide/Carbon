package net.draycia.carbon.managers;

import co.aikar.commands.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.commands.*;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;

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
                    completions.add(chatChannel.getName());
                }
            }

            return completions;
        });

        commandManager.getCommandCompletions().registerCompletion("channel", (context) -> {
            List<String> completions = new ArrayList<>();

            for (ChatChannel chatChannel : carbonChat.getChannelManager().getRegistry().values()) {
                    completions.add(chatChannel.getName());
            }

            return completions;
        });

        commandManager.getCommandContexts().registerContext(ChatChannel.class, (context) -> {
            String name = context.popFirstArg();

            for (ChatChannel chatChannel : carbonChat.getChannelManager().getRegistry().values()) {
                if (chatChannel.getName().equalsIgnoreCase(name)) {
                    return chatChannel;
                }
            }

            return null;
        });

        commandManager.getCommandContexts().registerContext(ChatUser.class, (context) -> {
            return carbonChat.getUserService().wrap(Bukkit.getOfflinePlayer(context.popFirstArg()));
        });

        commandManager.getCommandConditions().addCondition(ChatChannel.class,"canuse", (context, execution, value) -> {
            if (value == null) {
                throw new ConditionFailedException(carbonChat.getConfig().getString("language.cannot-use-channel"));
            }

            ChatUser user = carbonChat.getUserService().wrap(context.getIssuer().getPlayer());

            if (!value.canPlayerUse(user)) {
                throw new ConditionFailedException(value.getCannotUseMessage());
            }
        });

        commandManager.getCommandConditions().addCondition(ChatChannel.class,"exists", (context, execution, value) -> {
            if (value == null) {
                throw new ConditionFailedException(carbonChat.getConfig().getString("language.cannot-use-channel"));
            }
        });

        setupCommands(commandManager);
    }

    private void setupCommands(BukkitCommandManager manager) {
        manager.registerCommand(new ToggleCommand());
        manager.registerCommand(new ChannelCommand());
        manager.registerCommand(new IgnoreCommand());
        manager.registerCommand(new ChatReloadCommand());
        manager.registerCommand(new MeCommand());
        manager.registerCommand(new MessageCommand());
        manager.registerCommand(new ReplyCommand());
        manager.registerCommand(new SetColorCommand());
        manager.registerCommand(new SpyChannelCommand());
    }

    public co.aikar.commands.CommandManager getCommandManager() {
        return commandManager;
    }

}

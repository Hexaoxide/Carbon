package net.draycia.simplechat.managers;

import co.aikar.commands.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.commands.*;
import net.draycia.simplechat.storage.ChatUser;

import java.util.ArrayList;

public class CommandManager {

    private SimpleChat simpleChat;
    private BukkitCommandManager commandManager;

    public CommandManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        commandManager = new BukkitCommandManager(simpleChat);

        commandManager.getCommandCompletions().registerCompletion("chatchannel", (context) -> {
            ChatUser user = UserManager.wrap(context.getPlayer());

            ArrayList<String> completions = new ArrayList<>();

            for (ChatChannel chatChannel : simpleChat.getChannels()) {
                if (chatChannel.canPlayerUse(user)) {
                    completions.add(chatChannel.getName());
                }
            }

            return completions;
        });

        commandManager.getCommandContexts().registerContext(ChatChannel.class, (context) -> {
            String name = context.popFirstArg();

            for (ChatChannel chatChannel : simpleChat.getChannels()) {
                if (chatChannel.getName().equalsIgnoreCase(name)) {
                    return chatChannel;
                }
            }

            return null;
        });

        commandManager.getCommandConditions().addCondition(ChatChannel.class,"canuse", (context, execution, value) -> {
            ChatUser user = UserManager.wrap(context.getIssuer().getPlayer());

            if (!value.canPlayerUse(user)) {
                throw new ConditionFailedException(simpleChat.getConfig().getString("language.cannot-use-channel"));
            }
        });

        setupCommands(commandManager);
    }

    private void setupCommands(BukkitCommandManager manager) {
        manager.registerCommand(new ToggleCommand(simpleChat));
        manager.registerCommand(new ChannelCommand(simpleChat));
        manager.registerCommand(new IgnoreCommand(simpleChat));
        manager.registerCommand(new CommandClearChat(simpleChat));
        manager.registerCommand(new ShadowMuteCommand(simpleChat));
        manager.registerCommand(new ChatReloadCommand(simpleChat));
        manager.registerCommand(new MeCommand(simpleChat));
        manager.registerCommand(new MessageCommand(simpleChat));
        manager.registerCommand(new ReplyCommand(simpleChat));
    }

    public co.aikar.commands.CommandManager getCommandManager() {
        return commandManager;
    }

}

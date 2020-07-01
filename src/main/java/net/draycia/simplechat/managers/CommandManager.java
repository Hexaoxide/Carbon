package net.draycia.simplechat.managers;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.commands.*;

import java.util.ArrayList;

public class CommandManager {

    private SimpleChat simpleChat;

    public CommandManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        BukkitCommandManager manager = new BukkitCommandManager(simpleChat);

        manager.getCommandCompletions().registerCompletion("chatchannel", (context) -> {
            ArrayList<String> completions = new ArrayList<>();

            for (ChatChannel chatChannel : simpleChat.getChannels()) {
                if (chatChannel.canPlayerUse(context.getPlayer())) {
                    completions.add(chatChannel.getName());
                }
            }

            return completions;
        });

        manager.getCommandContexts().registerContext(ChatChannel.class, (context) -> {
            String name = context.popFirstArg();

            for (ChatChannel chatChannel : simpleChat.getChannels()) {
                if (chatChannel.getName().equalsIgnoreCase(name)) {
                    return chatChannel;
                }
            }

            return null;
        });

        manager.getCommandConditions().addCondition(ChatChannel.class,"canuse", (context, execution, value) -> {
            if (!value.canPlayerUse(context.getIssuer().getPlayer())) {
                throw new ConditionFailedException(simpleChat.getConfig().getString("language.cannot-use-channel"));
            }
        });

        setupCommands(manager);
    }

    private void setupCommands(BukkitCommandManager manager) {
        manager.registerCommand(new ToggleCommand(simpleChat));
        manager.registerCommand(new ChannelCommand(simpleChat));
        manager.registerCommand(new IgnoreCommand(simpleChat));
        manager.registerCommand(new CommandClearChat(simpleChat));
        manager.registerCommand(new ShadowMuteCommand(simpleChat));
        manager.registerCommand(new ChatReloadCommand(simpleChat));
        manager.registerCommand(new MeCommand(simpleChat));
    }

}

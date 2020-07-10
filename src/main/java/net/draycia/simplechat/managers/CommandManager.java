package net.draycia.simplechat.managers;

import co.aikar.commands.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.impls.AllianceChatChannel;
import net.draycia.simplechat.channels.impls.NationChatChannel;
import net.draycia.simplechat.channels.impls.PartyChatChannel;
import net.draycia.simplechat.channels.impls.TownChatChannel;
import net.draycia.simplechat.commands.*;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class CommandManager {

    private SimpleChat simpleChat;
    private BukkitCommandManager commandManager;

    public CommandManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        commandManager = new BukkitCommandManager(simpleChat);

        commandManager.enableUnstableAPI("help");

        commandManager.getCommandCompletions().registerCompletion("chatchannel", (context) -> {
            ChatUser user = simpleChat.getUserService().wrap(context.getPlayer());

            ArrayList<String> completions = new ArrayList<>();

            for (ChatChannel chatChannel : simpleChat.getChannels()) {
                if (chatChannel.canPlayerUse(user)) {
                    completions.add(chatChannel.getName());
                }
            }

            return completions;
        });

        commandManager.getCommandCompletions().registerCompletion("channel", (context) -> {
            ArrayList<String> completions = new ArrayList<>();

            for (ChatChannel chatChannel : simpleChat.getChannels()) {
                    completions.add(chatChannel.getName());
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

        commandManager.getCommandContexts().registerContext(ChatUser.class, (context) -> {
            return simpleChat.getUserService().wrap(Bukkit.getOfflinePlayer(context.popFirstArg()));
        });

        commandManager.getCommandConditions().addCondition(ChatChannel.class,"canuse", (context, execution, value) -> {
            if (value == null) {
                throw new ConditionFailedException(simpleChat.getConfig().getString("language.cannot-use-channel"));
            }

            ChatUser user = simpleChat.getUserService().wrap(context.getIssuer().getPlayer());

            if (!value.canPlayerUse(user)) {
                if (value.isTownChat() && !((TownChatChannel)value).isInTown(user)) {
                    throw new ConditionFailedException(simpleChat.getConfig().getString("language.town-cannot-use"));
                } else if (value.isNationChat() && !((NationChatChannel)value).isInNation(user)) {
                    throw new ConditionFailedException(simpleChat.getConfig().getString("language.nation-cannot-use"));
                } else if (value.isAllianceChat() && !((AllianceChatChannel)value).isInTown(user)) {
                    throw new ConditionFailedException(simpleChat.getConfig().getString("language.alliance-cannot-use"));
                } else if (value.isPartyChat() && !((PartyChatChannel)value).isInParty(user)) {
                    throw new ConditionFailedException(simpleChat.getConfig().getString("language.party-cannot-use"));
                } else {
                    throw new ConditionFailedException(simpleChat.getConfig().getString("language.cannot-use-channel"));
                }
            }
        });

        commandManager.getCommandConditions().addCondition(ChatChannel.class,"exists", (context, execution, value) -> {
            if (value == null) {
                throw new ConditionFailedException(simpleChat.getConfig().getString("language.cannot-use-channel"));
            }
        });

        setupCommands(commandManager);
    }

    private void setupCommands(BukkitCommandManager manager) {
        manager.registerCommand(new ToggleCommand(simpleChat));
        manager.registerCommand(new ChannelCommand(simpleChat));
        manager.registerCommand(new IgnoreCommand(simpleChat));
        manager.registerCommand(new ClearChatCommand(simpleChat));
        manager.registerCommand(new ShadowMuteCommand(simpleChat));
        manager.registerCommand(new ChatReloadCommand(simpleChat));
        manager.registerCommand(new MeCommand(simpleChat));
        manager.registerCommand(new MessageCommand(simpleChat));
        manager.registerCommand(new ReplyCommand(simpleChat));
        manager.registerCommand(new MuteCommand(simpleChat));
        manager.registerCommand(new ChannelColorCommand(simpleChat));
        manager.registerCommand(new SetColorCommand(simpleChat));
        manager.registerCommand(new SpyChannelCommand(simpleChat));
    }

    public co.aikar.commands.CommandManager getCommandManager() {
        return commandManager;
    }

}

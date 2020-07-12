package net.draycia.simplechatmoderation;

import co.aikar.commands.CommandManager;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechatmoderation.commands.ClearChatCommand;
import net.draycia.simplechatmoderation.commands.MuteCommand;
import net.draycia.simplechatmoderation.commands.ShadowMuteCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleChatModeration extends JavaPlugin {

    private SimpleChat simpleChat;

    @Override
    public void onEnable() {
        simpleChat = (SimpleChat)Bukkit.getPluginManager().getPlugin("SimpleChat");

        CommandManager commandManager = simpleChat.getCommandManager().getCommandManager();

        commandManager.registerCommand(new ClearChatCommand(this));
        commandManager.registerCommand(new MuteCommand(this));
        commandManager.registerCommand(new ShadowMuteCommand(this));
    }

    @Override
    public void onDisable() {

    }

    public SimpleChat getSimpleChat() {
        return simpleChat;
    }
}

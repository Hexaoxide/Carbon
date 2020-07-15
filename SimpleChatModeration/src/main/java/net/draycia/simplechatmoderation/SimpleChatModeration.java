package net.draycia.simplechatmoderation;

import co.aikar.commands.CommandManager;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechatmoderation.commands.ClearChatCommand;
import net.draycia.simplechatmoderation.commands.MuteCommand;
import net.draycia.simplechatmoderation.commands.ShadowMuteCommand;
import net.draycia.simplechatmoderation.listeners.CapsHandler;
import net.draycia.simplechatmoderation.listeners.FilterHandler;
import net.draycia.simplechatmoderation.listeners.MuteHandler;
import net.draycia.simplechatmoderation.listeners.ShadowMuteHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleChatModeration extends JavaPlugin {

    private SimpleChat simpleChat;

    @Override
    public void onEnable() {
        simpleChat = (SimpleChat)Bukkit.getPluginManager().getPlugin("SimpleChat");

        registerCommands();
        registerListeners();
    }

    private void registerCommands() {
        CommandManager commandManager = simpleChat.getCommandManager().getCommandManager();

        commandManager.registerCommand(new ClearChatCommand(this));
        commandManager.registerCommand(new MuteCommand(this));
        commandManager.registerCommand(new ShadowMuteCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new CapsHandler(this), this);
        Bukkit.getPluginManager().registerEvents(new FilterHandler(this), this);
        Bukkit.getPluginManager().registerEvents(new MuteHandler(), this);
        Bukkit.getPluginManager().registerEvents(new ShadowMuteHandler(), this);
    }

    @Override
    public void onDisable() {

    }

    public SimpleChat getSimpleChat() {
        return simpleChat;
    }
}

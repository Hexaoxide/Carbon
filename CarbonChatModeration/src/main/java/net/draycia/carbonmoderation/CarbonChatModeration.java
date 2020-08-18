package net.draycia.carbonmoderation;

import net.draycia.carbon.libs.co.aikar.commands.BukkitCommandManager;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbonmoderation.commands.ClearChatCommand;
import net.draycia.carbonmoderation.commands.ModReloadCommand;
import net.draycia.carbonmoderation.commands.MuteCommand;
import net.draycia.carbonmoderation.commands.ShadowMuteCommand;
import net.draycia.carbonmoderation.listeners.CapsHandler;
import net.draycia.carbonmoderation.listeners.FilterHandler;
import net.draycia.carbonmoderation.listeners.MuteHandler;
import net.draycia.carbonmoderation.listeners.ShadowMuteHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CarbonChatModeration extends JavaPlugin {

    private CarbonChat carbonChat;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        carbonChat = (CarbonChat)Bukkit.getPluginManager().getPlugin("CarbonChat");

        registerCommands();
        registerListeners();
    }

    private void registerCommands() {
        BukkitCommandManager commandManager = carbonChat.getCommandManager().getCommandManager();

        commandManager.registerCommand(new ClearChatCommand(this));
        commandManager.registerCommand(new MuteCommand(this));
        commandManager.registerCommand(new ShadowMuteCommand(this));
        commandManager.registerCommand(new ModReloadCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new CapsHandler(this), this);
        Bukkit.getPluginManager().registerEvents(new FilterHandler(this), this);
        Bukkit.getPluginManager().registerEvents(new MuteHandler(), this);
        Bukkit.getPluginManager().registerEvents(new ShadowMuteHandler(this), this);
    }

    @Override
    public void onDisable() {

    }

    public CarbonChat getCarbonChat() {
        return carbonChat;
    }
}

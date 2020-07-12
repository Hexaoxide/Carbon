package net.draycia.simplechatmoderation;

import net.draycia.simplechat.SimpleChat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleChatModeration extends JavaPlugin {

    private SimpleChat simpleChat;

    @Override
    public void onEnable() {
        simpleChat = (SimpleChat)Bukkit.getPluginManager().getPlugin("SimpleChat");
    }

    @Override
    public void onDisable() {

    }

    public SimpleChat getSimpleChat() {
        return simpleChat;
    }
}

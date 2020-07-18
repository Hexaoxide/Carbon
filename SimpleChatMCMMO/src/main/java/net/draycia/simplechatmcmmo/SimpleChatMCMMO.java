package net.draycia.simplechatmcmmo;

import net.draycia.simplechat.events.ChannelRegisterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleChatMCMMO extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onChannelRegister(ChannelRegisterEvent event) {

    }

}

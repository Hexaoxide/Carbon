package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final CarbonChat carbonChat;

    public PlayerJoinListener(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());

        if (user.getNickname() != null) {
            user.setNickname(user.getNickname());
        }
    }

}

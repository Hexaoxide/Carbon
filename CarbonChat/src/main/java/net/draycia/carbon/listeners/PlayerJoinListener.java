package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final CarbonChat carbonChat;

    public PlayerJoinListener(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());

        if (user.getNickname() == null) {
            return;
        }

        Component component = carbonChat.getAdventureManager().processMessage(user.getNickname());
        String nickname = LegacyComponentSerializer.legacySection().serialize(component);

        event.getPlayer().setDisplayName(nickname);
    }

}

package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlayerJoinListener implements Listener {

    @NonNull
    private final CarbonChat carbonChat;

    public PlayerJoinListener(@NonNull CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());

        carbonChat.getUserService().validate(user);

        if (user.getNickname() != null) {
            user.setNickname(user.getNickname());
        }

        String channel = carbonChat.getConfig().getString("channel-on-join");

        if (channel == null || channel.isEmpty()) {
            return;
        }

        if (channel.equals("DEFAULT")) {
            user.setSelectedChannel(carbonChat.getChannelManager().getDefaultChannel());
            return;
        }

        ChatChannel chatChannel = carbonChat.getChannelManager().getRegistry().get(channel);

        if (chatChannel != null) {
            user.setSelectedChannel(chatChannel);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ChatUser user = carbonChat.getUserService().wrapIfLoaded(event.getPlayer());

        if (user != null) {
            carbonChat.getUserService().invalidate(user);
        }
    }

}

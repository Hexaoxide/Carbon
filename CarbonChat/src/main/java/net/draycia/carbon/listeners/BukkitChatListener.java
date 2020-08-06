package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class BukkitChatListener implements Listener {

    private final CarbonChat carbonChat;

    public BukkitChatListener(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    // Chat messages
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerchat(AsyncPlayerChatEvent event) {
        event.getRecipients().clear();

        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());

        // TODO: ChatUser#canUseChannel(ChatChannel channel)
        if (!user.getSelectedChannel().canPlayerUse(user)) {
            return;
        }

        if (event.isAsynchronous()) {
            user.getSelectedChannel().sendMessage(user, event.getMessage(), false);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                user.getSelectedChannel().sendMessage(user, event.getMessage(), false);
            });
        }
    }

}

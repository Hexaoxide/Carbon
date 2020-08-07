package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
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
        ChatChannel channel = user.getSelectedChannel();

        for (ChatChannel entry : carbonChat.getChannelManager().getRegistry().values()) {
            if (entry.getMessagePrefix() == null) {
                continue;
            }

            if (event.getMessage().startsWith(entry.getMessagePrefix())) {
                if (entry.canPlayerUse(user)) {
                    event.setMessage(event.getMessage().substring(entry.getMessagePrefix().length()));
                    channel = entry;
                    break;
                }
            }
        }

        final ChatChannel selectedChannel = channel;

        if (!selectedChannel.canPlayerUse(user)) {
            return;
        }

        if (event.isAsynchronous()) {
            selectedChannel.sendMessage(user, event.getMessage(), false);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                selectedChannel.sendMessage(user, event.getMessage(), false);
            });
        }
    }

}

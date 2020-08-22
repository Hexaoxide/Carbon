package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BukkitChatListener implements Listener {

    private final CarbonChat carbonChat;

    public BukkitChatListener(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    // Chat messages
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerchat(AsyncPlayerChatEvent event) {
        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());
        ChatChannel channel = user.getSelectedChannel();

        if (channel.shouldCancelChatEvent()) {
            event.setCancelled(true);
        }

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

        final Collection<ChatUser> recipients;

        if (selectedChannel.honorsRecipientList()) {
            recipients = new HashSet<>();

            for (Player recipient : event.getRecipients()) {
                recipients.add(carbonChat.getUserService().wrap(recipient));
            }
        } else {
            recipients = (List<ChatUser>)selectedChannel.audiences();
        }

        event.getRecipients().clear();

        if (event.isAsynchronous()) {
            selectedChannel.sendMessage(user, recipients, event.getMessage(), false);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                selectedChannel.sendMessage(user, recipients, event.getMessage(), false);
            });
        }
    }

}

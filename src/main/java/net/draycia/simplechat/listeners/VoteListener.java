package net.draycia.simplechat.listeners;

import com.vexsoftware.votifier.model.VotifierEvent;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.javacord.api.DiscordApi;

public class VoteListener implements Listener {

    private SimpleChat simpleChat;

    public VoteListener(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVote(VotifierEvent event) {
        if (!simpleChat.getConfig().getBoolean("listeners.vote")) {
            return;
        }

        ChatChannel chatChannel = simpleChat.getDefaultChannel();

        if (chatChannel != null && chatChannel.getChannelId() > -1) {
            DiscordApi api = simpleChat.getDiscordManager().getDiscordAPI();

            String message = simpleChat.getConfig().getString("language.vote-received")
                    .replace("<service>", event.getVote().getServiceName())
                    .replace("<player>", event.getVote().getUsername());

            api.getServerTextChannelById(chatChannel.getChannelId()).ifPresent(channel -> {
                channel.sendMessage(message);
            });
        }
    }

}

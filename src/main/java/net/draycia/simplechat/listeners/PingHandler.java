package net.draycia.simplechat.listeners;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.events.ChatComponentEvent;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PingHandler implements Listener {

    private SimpleChat simpleChat;

    public PingHandler(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPing(ChatComponentEvent event) {
        for (ChatUser user : event.getRecipients()) {
            if (user.getUUID().equals(event.getUser().getUUID())) {
                continue;
            }

            if (event.getOriginalMessage().contains(user.asPlayer().getName())) {
                if (simpleChat.getConfig().getBoolean("pings.enabled")) {
                    Key key = Key.of(simpleChat.getConfig().getString("pings.sound"));
                    Sound.Source source = Sound.Source.valueOf(simpleChat.getConfig().getString("pings.source"));
                    float volume = (float)simpleChat.getConfig().getDouble("pings.volume");
                    float pitch = (float)simpleChat.getConfig().getDouble("pings.pitch");

                    user.playSound(Sound.of(key, source, volume, pitch));
                }
            }
        }
    }

}

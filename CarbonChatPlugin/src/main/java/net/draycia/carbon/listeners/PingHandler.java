package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PingHandler implements Listener {

    private CarbonChat carbonChat;

    public PingHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPing(ChatComponentEvent event) {
        for (ChatUser user : event.getRecipients()) {
            if (user.getUUID().equals(event.getUser().getUUID())) {
                continue;
            }

            if (event.getOriginalMessage().contains(user.asPlayer().getName())) {
                if (carbonChat.getConfig().getBoolean("pings.enabled")) {
                    Key key = Key.of(carbonChat.getConfig().getString("pings.sound"));
                    Sound.Source source = Sound.Source.valueOf(carbonChat.getConfig().getString("pings.source"));
                    float volume = (float) carbonChat.getConfig().getDouble("pings.volume");
                    float pitch = (float) carbonChat.getConfig().getDouble("pings.pitch");

                    user.playSound(Sound.of(key, source, volume, pitch));
                }
            }
        }
    }

}

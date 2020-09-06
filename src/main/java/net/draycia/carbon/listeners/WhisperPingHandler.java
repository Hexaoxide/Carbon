package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PrivateMessageEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class WhisperPingHandler implements Listener {

    private final CarbonChat carbonChat;

    public WhisperPingHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPing(@NonNull PrivateMessageEvent event) {
        if (event.getTarget() == null) {
            return;
        }

        if (event.getSender().getUUID().equals(event.getTarget().getUUID())) {
            return;
        }

        String senderName = event.getSender().asOfflinePlayer().getName();

        if (senderName == null || !event.getMessage().contains(senderName)) {
            return;
        }

        if (!carbonChat.getConfig().getBoolean("whisper.pings.enabled")) {
            return;
        }

        Key key = Key.of(carbonChat.getConfig().getString("whisper.pings.sound"));
        Sound.Source source = Sound.Source.valueOf(carbonChat.getConfig().getString("whisper.pings.source"));
        float volume = (float) carbonChat.getConfig().getDouble("whisper.pings.volume");
        float pitch = (float) carbonChat.getConfig().getDouble("whisper.pings.pitch");

        event.getSender().playSound(Sound.of(key, source, volume, pitch));
    }

}

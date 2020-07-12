package net.draycia.simplechat.listeners;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.events.ChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CapsHandler implements Listener {

    private SimpleChat simpleChat;

    public CapsHandler(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMessage(ChatFormatEvent event) {
        if (!simpleChat.getConfig().getBoolean("caps-protection.enabled")) {
            return;
        }

        if (!(event.getMessage().length() >= simpleChat.getConfig().getInt("caps-protection.minimum-length"))) {
            return;
        }

        int amountOfCaps = 0;

        for (char letter : event.getMessage().toCharArray()) {
            if (Character.isUpperCase(letter)) {
                amountOfCaps++;
            }
        }

        double capsPercentage = (amountOfCaps * 100.0) / event.getMessage().length();

        if (!(capsPercentage >= simpleChat.getConfig().getDouble("caps-protection.percent-caps"))) {
            return;
        }

        if (simpleChat.getConfig().getBoolean("block-message")) {
            event.setCancelled(true);
        } else {
            event.setMessage(event.getMessage().toLowerCase());
        }
    }

}

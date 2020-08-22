package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PreChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CapsHandler implements Listener {

    private final CarbonChat carbonChat;

    public CapsHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMessage(PreChatFormatEvent event) {
        if (!carbonChat.getModConfig().getBoolean("caps-protection.enabled")) {
            return;
        }

        if (!(event.getMessage().length() >= carbonChat.getModConfig().getInt("caps-protection.minimum-length"))) {
            return;
        }

        int amountOfCaps = 0;

        for (char letter : event.getMessage().toCharArray()) {
            if (Character.isUpperCase(letter)) {
                amountOfCaps++;
            }
        }

        double capsPercentage = (amountOfCaps * 100.0) / event.getMessage().length();

        if (!(capsPercentage >= carbonChat.getModConfig().getDouble("caps-protection.percent-caps"))) {
            return;
        }

        if (carbonChat.getModConfig().getBoolean("block-message")) {
            event.setCancelled(true);
        } else {
            event.setMessage(event.getMessage().toLowerCase());
        }
    }

}

package net.draycia.carbonmoderation.listeners;

import net.draycia.carbon.events.PreChatFormatEvent;
import net.draycia.carbonmoderation.CarbonChatModeration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CapsHandler implements Listener {

    private final CarbonChatModeration moderation;

    public CapsHandler(CarbonChatModeration moderation) {
        this.moderation = moderation;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMessage(PreChatFormatEvent event) {
        if (!moderation.getConfig().getBoolean("caps-protection.enabled")) {
            return;
        }

        if (!(event.getMessage().length() >= moderation.getConfig().getInt("caps-protection.minimum-length"))) {
            return;
        }

        int amountOfCaps = 0;

        for (char letter : event.getMessage().toCharArray()) {
            if (Character.isUpperCase(letter)) {
                amountOfCaps++;
            }
        }

        double capsPercentage = (amountOfCaps * 100.0) / event.getMessage().length();

        if (!(capsPercentage >= moderation.getConfig().getDouble("caps-protection.percent-caps"))) {
            return;
        }

        if (moderation.getConfig().getBoolean("block-message")) {
            event.setCancelled(true);
        } else {
            event.setMessage(event.getMessage().toLowerCase());
        }
    }

}

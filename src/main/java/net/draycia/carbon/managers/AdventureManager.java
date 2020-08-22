package net.draycia.carbon.managers;

import de.themoep.minedown.adventure.MineDown;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.CarbonChat;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class AdventureManager {

    private final CarbonChat carbonChat;
    private final BukkitAudiences audiences;

    public AdventureManager(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
        this.audiences = BukkitAudiences.create(carbonChat);
    }

    public Component processMessageWithPapi(Player player, String input, String... placeholders) {
        return processMessage(PlaceholderAPI.setPlaceholders(player, input), placeholders);
    }

    public Component processMessage(String input, String... placeholders) {
        switch (carbonChat.getLanguage().getString("formatting.type", "minimessage").toLowerCase()) {
            case "minedown":
                return processMineDown(input, placeholders);
            case "minimessage-markdown":
                return MiniMessage.markdown().parse(input, placeholders);
            case "minimessage":
            default:
                return MiniMessage.get().parse(input, placeholders);
        }
    }

    private Component processMineDown(String input, String... placeholders) {
        return MineDown.parse(input, placeholders);
    }

    public BukkitAudiences getAudiences() {
        return audiences;
    }
}

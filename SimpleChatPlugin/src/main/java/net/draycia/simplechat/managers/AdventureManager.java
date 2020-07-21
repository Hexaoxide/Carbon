package net.draycia.simplechat.managers;

import de.themoep.minedown.MineDown;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeCordComponentSerializer;
import org.bukkit.entity.Player;

public class AdventureManager {

    private final SimpleChat simpleChat;
    private final BukkitAudiences audiences;

    public AdventureManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
        this.audiences = BukkitAudiences.create(simpleChat);
    }

    public Component processMessageWithPapi(Player player, String input, String... placeholders) {
        return processMessage(PlaceholderAPI.setPlaceholders(player, input), placeholders);
    }

    public Component processMessage(String input, String... placeholders) {
        switch (simpleChat.getConfig().getString("formatting.type", "minimessage").toLowerCase()) {
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
        return BungeeCordComponentSerializer.get().deserialize(MineDown.parse(input, placeholders));
    }

    public BukkitAudiences getAudiences() {
        return audiences;
    }
}

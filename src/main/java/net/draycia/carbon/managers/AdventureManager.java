package net.draycia.carbon.managers;

import de.themoep.minedown.adventure.MineDown;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.CarbonChat;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AdventureManager {

    private final @NonNull CarbonChat carbonChat;
    private final @NonNull BukkitAudiences audiences;

    public AdventureManager(@NonNull CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
        this.audiences = BukkitAudiences.create(carbonChat);
    }

    public Component processMessageWithPapi(@NonNull Player player, @Nullable String input, @NonNull String @NonNull ... placeholders) {
        if (input == null || input.trim().isEmpty()) {
            return TextComponent.empty();
        }

        return processMessage(PlaceholderAPI.setPlaceholders(player, input), placeholders);
    }

    public Component processMessage(@Nullable String input, @NonNull String @NonNull ... placeholders) {
        if (input == null || input.trim().isEmpty()) {
            return TextComponent.empty();
        }

        switch (carbonChat.getLanguage().getString("formatting.type", "minimessage").toLowerCase()) {
            case "minedown":
                return processMineDown(input, placeholders);
            case "mojang":
            case "mojangson":
            case "json":
                return processMojang(input, placeholders);
            case "minimessage-markdown":
                return MiniMessage.markdown().parse(input, placeholders);
            case "minimessage":
            default:
                return MiniMessage.get().parse(input, placeholders);
        }
    }

    @NonNull
    private Component processMojang(String input, String @NonNull ... placeholders) {
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = placeholders[i];
            String replacement = placeholders[i + 1];

            input = input.replace("<" + placeholder + ">", replacement);
        }

        return getAudiences().gsonSerializer().deserialize(input);
    }

    private Component processMineDown(String input, String... placeholders) {
        return MineDown.parse(input, placeholders);
    }

    public @NonNull BukkitAudiences getAudiences() {
        return audiences;
    }
}

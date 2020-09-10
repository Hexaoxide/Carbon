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

  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private final BukkitAudiences audiences;

  public AdventureManager(@NonNull CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.audiences = BukkitAudiences.create(carbonChat);
  }

  @NonNull
  public Component processMessageWithPapi(@NonNull Player player, @Nullable String input, @NonNull String @NonNull ... placeholders) {
    if (input == null || input.trim().isEmpty()) {
      return TextComponent.empty();
    }

    return processMessage(PlaceholderAPI.setPlaceholders(player, input), placeholders);
  }

  @NonNull
  public Component processMessage(@Nullable String input, @NonNull String @NonNull ... placeholders) {
    if (input == null || input.trim().isEmpty()) {
      return TextComponent.empty();
    }

    switch (carbonChat.getLanguage().getString("formatting.type", "minimessage").toLowerCase()) {
      case "minedown":
        return MineDown.parse(input, placeholders);
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
  private Component processMojang(@NonNull String input, @NonNull String @NonNull ... placeholders) {
    for (int i = 0; i < placeholders.length; i += 2) {
      String placeholder = placeholders[i];
      String replacement = placeholders[i + 1];

      input = input.replace("<" + placeholder + ">", replacement);
    }

    return getAudiences().gsonSerializer().deserialize(input);
  }

  @NonNull
  public BukkitAudiences getAudiences() {
    return audiences;
  }
}

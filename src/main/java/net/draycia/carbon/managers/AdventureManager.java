package net.draycia.carbon.managers;

import de.themoep.minedown.adventure.MineDown;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.craftbukkit.BukkitComponentSerializer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AdventureManager {

  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private final BukkitAudiences audiences;

  public AdventureManager(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.audiences = BukkitAudiences.create(carbonChat);
  }

  @NonNull
  public Component processMessageWithPapi(@NonNull final Player player, @Nullable final String input,
                                          @NonNull final String @NonNull ... placeholders) {
    if (input == null || input.trim().isEmpty()) {
      return TextComponent.empty();
    }

    return this.processMessage(PlaceholderAPI.setPlaceholders(player, input), placeholders);
  }

  @NonNull
  public Component processMessage(@Nullable final String input, @NonNull final String @NonNull ... placeholders) {
    if (input == null || input.trim().isEmpty()) {
      return TextComponent.empty();
    }

    final String format = CarbonUtils.translateAlternateColors(input);

    switch (this.carbonChat.language().getString("formatting.type", "minimessage").toLowerCase()) {
      case "minedown":
        return MineDown.parse(format, placeholders);
      case "mojang":
      case "mojangson":
      case "json":
        return this.processMojang(format, placeholders);
      case "minimessage-markdown":
        return MiniMessage.markdown().parse(format, placeholders);
      case "minimessage":
      default:
        return MiniMessage.get().parse(format, placeholders);
    }
  }

  @NonNull
  private Component processMojang(@NonNull String input, @NonNull final String @NonNull ... placeholders) {
    for (int i = 0; i < placeholders.length; i += 2) {
      final String placeholder = placeholders[i];
      final String replacement = placeholders[i + 1];

      input = input.replace("<" + placeholder + ">", replacement);
    }

    return BukkitComponentSerializer.gson().deserialize(input);
  }

  @NonNull
  public BukkitAudiences audiences() {
    return this.audiences;
  }
}

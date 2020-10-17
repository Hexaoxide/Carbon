package net.draycia.carbon.common.adventure;

import de.themoep.minedown.adventure.MineDown;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.adventure.FormatType;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.common.utils.ColorUtils;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AdventureManager implements MessageProcessor {

  private @NonNull final AudienceProvider provider;

  private @NonNull final CarbonChat carbonChat;

  private @NonNull final FormatType formatType;

  public AdventureManager(@NonNull final AudienceProvider provider, @NonNull final FormatType formatType) {
    this.provider = provider;
    this.formatType = formatType;
    this.carbonChat = CarbonChatProvider.carbonChat();
  }

  @Override
  public @NonNull Component processMessage(@Nullable final String input, @NonNull final String @NonNull ... placeholders) {
    if (input == null || input.trim().isEmpty()) {
      return Component.empty();
    }

    final String format = ColorUtils.translateAlternateColors(input)
      .replace("\\n", "\n")
      .replace("<br>", "\n")
      .replace("<server>", this.carbonChat.carbonSettings().serverName());

    switch (this.formatType) {
      case MINEDOWN:
        return MineDown.parse(format, placeholders);
      case MOJANG:
        return this.processMojang(format, placeholders);
      case MINIMESSAGE_MARKDOWN:
        return MiniMessage.markdown().parse(format, placeholders);
      case MINIMESSAGE:
      default:
        return MiniMessage.get().parse(format, placeholders);
    }
  }

  private @NonNull Component processMojang(@NonNull String input, @NonNull final String @NonNull ... placeholders) {
    for (int i = 0; i < placeholders.length; i += 2) {
      final String placeholder = placeholders[i];
      final String replacement = placeholders[i + 1];

      input = input.replace("<" + placeholder + ">", replacement);
    }

    return this.carbonChat.gsonSerializer().deserialize(input);
  }

  @Override
  public @NonNull FormatType formatType() {
    return this.formatType;
  }

  @Override
  public @NonNull AudienceProvider audiences() {
    return this.provider;
  }
}

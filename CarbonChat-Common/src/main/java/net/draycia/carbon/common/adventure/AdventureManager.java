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

  private final @NonNull AudienceProvider provider;

  private final @NonNull CarbonChat carbonChat;

  private final @NonNull FormatType formatType;

  public AdventureManager(final @NonNull AudienceProvider provider, final @NonNull FormatType formatType) {
    this.provider = provider;
    this.formatType = formatType;
    this.carbonChat = CarbonChatProvider.carbonChat();
  }

  @Override
  public @NonNull Component processMessage(final @Nullable String input, final @NonNull String @NonNull ... placeholders) {
    if (input == null || input.trim().isEmpty()) {
      return Component.empty();
    }

    String format = ColorUtils.translateAlternateColors(input)
      .replace("\\n", "\n")
      .replace("<br>", "\n")
      .replace("<server>", this.carbonChat.carbonSettings().serverName());

    format = this.processPlaceholders(format, placeholders);

    switch (this.formatType) {
      case MINEDOWN:
        return MineDown.parse(format);
      case MOJANG:
        return this.processMojang(format);
      case MINIMESSAGE_MARKDOWN:
        return MiniMessage.markdown().parse(format);
      case MINIMESSAGE:
      default:
        return MiniMessage.get().parse(format);
    }
  }

  private @NonNull Component processMojang(final @NonNull String input) {
    return this.carbonChat.gsonSerializer().deserialize(input);
  }

  private @NonNull String processPlaceholders(@NonNull String input, final @NonNull String @NonNull ... placeholders) {
    for (int i = 0; i < placeholders.length; i += 2) {
      final String placeholder = placeholders[i];
      final String replacement = placeholders[i + 1];

      input = input.replace("<" + placeholder + ">", replacement);
    }

    return input;
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

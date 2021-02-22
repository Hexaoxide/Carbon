package net.draycia.carbon.common.adventure;

import de.themoep.minedown.adventure.MineDown;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.adventure.FormatType;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.common.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AdventureManager implements MessageProcessor {

  private final @NonNull CarbonChat carbonChat;

  private final @NonNull FormatType formatType;

  public AdventureManager(final @NonNull FormatType formatType) {
    this.formatType = formatType;
    this.carbonChat = CarbonChatProvider.carbonChat();
  }

  @Override
  public @NonNull Component processMessage(final @Nullable String input, final @NonNull Template @NonNull ... templates) {
    if (input == null || input.trim().isEmpty()) {
      return Component.empty();
    }

    String format = ColorUtils.translateAlternateColors(input)
      .replace("\\n", "\n")
      .replace("<br>", "\n")
      .replace("<server>", this.carbonChat.carbonSettings().serverName());

    format = this.processPlaceholders(format, templates);

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

  private @NonNull String processPlaceholders(@NonNull String input, final @NonNull Template @NonNull ... templates) {
    for (final Template template : templates) {
      if (template instanceof Template.StringTemplate) {
        final Template.StringTemplate stringTemplate = (Template.StringTemplate) template;
        input = input.replace("<" + stringTemplate.key() + ">", stringTemplate.value());
      } else if (template instanceof Template.ComponentTemplate) {
        final Template.ComponentTemplate componentTemplate = (Template.ComponentTemplate) template;
        input = input.replace("<" + componentTemplate.key() + ">", MiniMessage.get().serialize(componentTemplate.value()));
      }
    }

    return input;
  }

  @Override
  public @NonNull FormatType formatType() {
    return this.formatType;
  }

}

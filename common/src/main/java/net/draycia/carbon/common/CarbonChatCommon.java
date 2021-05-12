package net.draycia.carbon.common;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonEventHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public abstract class CarbonChatCommon implements CarbonChat {

  private final CarbonEventHandler eventHandler = new CarbonEventHandler();

  public void initialize() {

  }

  @Override
  public @NonNull CarbonEventHandler eventHandler() {
    return this.eventHandler;
  }

  @Override
  public @NonNull Component createItemHoverComponent(
    final @NonNull Component displayName,
    final @NonNull HoverEventSource<HoverEvent.ShowItem> itemStack
  ) {
    final TextComponent.Builder builder = text(); // Empty root - prevents style leaking.

    builder.hoverEvent(itemStack); // Let this be inherited by all coming components.

    builder.append(text("[", WHITE));
    builder.append(displayName);
    builder.append(text("]", WHITE));

    return builder.build();
  }

}

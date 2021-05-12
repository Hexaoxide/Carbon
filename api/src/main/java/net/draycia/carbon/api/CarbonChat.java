package net.draycia.carbon.api;

import net.draycia.carbon.api.events.CarbonEventHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public interface CarbonChat {

  @NonNull Logger logger();

  @NonNull CarbonEventHandler eventHandler();

  @NonNull Component createItemHoverComponent(final @NonNull UUID uuid);

  // TODO: Move this to Common
  default @NonNull Component createItemHoverComponent(
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

package net.draycia.carbon.api;

import net.draycia.carbon.api.events.CarbonEventHandler;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public interface CarbonChat {

  @NonNull Logger logger();

  @NonNull CarbonEventHandler eventHandler();

  @NonNull UserManager userManager();

  @NonNull Component createItemHoverComponent(final @NonNull UUID uuid);

  @NonNull Component createItemHoverComponent(
    final @NonNull Component displayName,
    final @NonNull HoverEventSource<HoverEvent.ShowItem> itemStack
  );

}

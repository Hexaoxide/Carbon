package net.draycia.carbon.api.util;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
public interface PlayerComponentRenderer {
  /**
   * Renders a Component for the specified recipient.
   * @param sender The player that sent the message.
   * @param recipient The player receiving the message.
   * @param message The message being sent.
   * @return The component to be shown to the recipient,
   * or null if the recipient should not receive the message.
   */
  @Nullable
  Component render(final @NonNull CarbonPlayer sender,
                   final @NonNull CarbonPlayer recipient,
                   final @NonNull Component message);
}

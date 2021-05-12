package net.draycia.carbon.api.users;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface CarbonPlayer extends Audience, Identified {

  /**
   * @return The player's username.
   */
  @NonNull String username();

  /**
   * @return The player's display name, shown in places like chat and tab menu.
   */
  @NonNull Component displayName();

  void displayName(final @Nullable Component displayName);

  /**
   * @return The player's UUID.
   */
  @NonNull UUID uuid();

  @NonNull Component createItemHoverComponent();

}

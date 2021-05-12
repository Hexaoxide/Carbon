package net.draycia.carbon.api.users;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface CarbonPlayer extends Audience, Identified {

  /**
   * Gets the player's username.
   *
   * @return The player's username.
   */
  @NonNull String username();

  /**
   * Gets the player's display name, shown in places like chat and tab menu.
   *
   * @return The player's display name.
   */
  @NonNull Component displayName();

  void displayName(final @Nullable Component displayName);

  /**
   * Gets the player's UUID.
   *
   * @return The player's UUID.
   */
  @NonNull UUID uuid();

  @NonNull Component createItemHoverComponent();

}

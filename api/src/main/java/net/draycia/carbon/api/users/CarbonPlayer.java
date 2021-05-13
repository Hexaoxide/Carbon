package net.draycia.carbon.api.users;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

/**
 * Generic abstraction for players.
 *
 * @since 2.0.0
 */
public interface CarbonPlayer extends Audience, Identified {

  /**
   * Gets the player's username.
   *
   * @return the player's username
   *
   * @since 2.0.0
   */
  @NonNull String username();

  /**
   * Gets the player's display name, shown in places like chat and tab menu.
   *
   * @return the player's display name
   *
   * @since 2.0.0
   */
  @NonNull Component displayName();

  /**
   * Sets the player's display name.<br>
   * Setting null is equivalent to setting the display name to the username.
   *
   * @param displayName the new display name
   *
   * @since 2.0.0
   */
  void displayName(final @Nullable Component displayName);

  /**
   * The player's UUID, often used for identification purposes.
   *
   * @return the player's UUID
   *
   * @since 2.0.0
   */
  @NonNull UUID uuid();

  /**
   * Creates a {@link Component} with a content and item hover given the player's actively held item.
   *
   * @return the player's held item as an item hover component
   *
   * @since 2.0.0
   */
  @NonNull Component createItemHoverComponent();

}

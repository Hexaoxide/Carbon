package net.draycia.carbon.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Static accessor for the {@link CarbonChat} class.
 *
 * @since 1.0.0
 */
public final class CarbonChatProvider {

  private CarbonChatProvider() {

  }

  private static @Nullable CarbonChat instance;

  /**
   * Registers the {@link CarbonChat} implementation.
   *
   * @param carbonChat the carbon implementation
   *
   * @since 1.0.0
   */
  public static void register(final @NonNull CarbonChat carbonChat) {
    CarbonChatProvider.instance = carbonChat;
  }

  /**
   * Gets the currently registered {@link CarbonChat} implementation.
   *
   * @return the registered carbon implementation
   *
   * @since 1.0.0
   */
  public static @NonNull CarbonChat carbonChat() {
    if (CarbonChatProvider.instance == null) {
      throw new IllegalStateException("CarbonChat not initialized!"); // LuckPerms design go brrrr
    }

    return CarbonChatProvider.instance;
  }

}

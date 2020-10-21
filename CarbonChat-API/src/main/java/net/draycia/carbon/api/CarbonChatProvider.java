package net.draycia.carbon.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class CarbonChatProvider {

  private CarbonChatProvider() {
  }

  private static @Nullable CarbonChat instance;

  public static void register(final @NonNull CarbonChat carbonChat) {
    CarbonChatProvider.instance = carbonChat;
  }

  public static @NonNull CarbonChat carbonChat() {
    if (CarbonChatProvider.instance == null) {
      throw new IllegalStateException("CarbonChat not initialized!"); // LuckPerms design go brrrr
    }

    return CarbonChatProvider.instance;
  }

}

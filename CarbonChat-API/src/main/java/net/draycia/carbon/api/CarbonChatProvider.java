package net.draycia.carbon.api;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class CarbonChatProvider {

  private CarbonChatProvider() {

  }

  private static CarbonChat instance;

  public static void register(final @NonNull CarbonChat carbonChat) {
    CarbonChatProvider.instance = carbonChat;
  }

  public static CarbonChat carbonChat() {
    return CarbonChatProvider.instance;
  }

}

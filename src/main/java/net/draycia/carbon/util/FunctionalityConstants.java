package net.draycia.carbon.util;

import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("ResultOfMethodCallIgnored") // This is a common pattern in this class.
public final class FunctionalityConstants {

  public static final boolean HAS_HOVER_EVENT_METHOD =
      noThrow(() -> ItemFactory.class.getMethod("hoverContentOf", ItemStack.class));

  private static boolean noThrow(@NonNull ThrowingRunnable runnable) {
    try {
      runnable.run();
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}

package net.draycia.carbon.bukkit.util;

import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Field;

@SuppressWarnings("ResultOfMethodCallIgnored") // This is a common pattern in this class.
public final class FunctionalityConstants {

  public static final boolean HAS_HOVER_EVENT_METHOD = noThrow(() -> ItemFactory.class.getMethod("hoverContentOf", ItemStack.class));
  public static final boolean HAS_PROXY;

  static {
    boolean hasProxy = false;

    try {
      final Class<?> spigotConfig = Class.forName("org.spigotmc.SpigotConfig");
      final Field bungee = spigotConfig.getField("bungee");
      hasProxy = bungee.getBoolean(null);
    } catch (final ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
    }

    HAS_PROXY = hasProxy;
  }

  private FunctionalityConstants() {

  }

  private static boolean noThrow(final @NonNull ThrowingRunnable runnable) {
    try {
      runnable.run();
      return true;
    } catch (final Exception ex) {
      return false;
    }
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}

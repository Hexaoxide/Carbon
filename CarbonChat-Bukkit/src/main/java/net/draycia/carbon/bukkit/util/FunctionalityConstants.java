package net.draycia.carbon.bukkit.util;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Field;

@SuppressWarnings("argument.type.incompatible")
public final class FunctionalityConstants {

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

package net.draycia.carbon.common;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class Injector {

  private Injector() {

  }

  private static final Map<Class<?>, Object> providers = new HashMap<>();

  public static <T> T byInject(final @NonNull Class<T> clazz) {
    return (T) providers.get(clazz);
  }

  public static <T> void provide(final @NonNull Class<T> clazz, final @NonNull T provider) {
    providers.put(clazz, provider);
  }

}

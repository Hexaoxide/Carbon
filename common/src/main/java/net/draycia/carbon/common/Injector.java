package net.draycia.carbon.common;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

// TODO: remove
public final class Injector {

  private Injector() {

  }

  private static final Map<Class<?>, Object> providers = new HashMap<>();

  @Deprecated(forRemoval = true)
  public static <T, S extends T> S byInject(final @NonNull Class<T> clazz) {
    return (S) providers.get(clazz);
  }

  @Deprecated(forRemoval = true)
  public static <T> void provide(final @NonNull Class<T> clazz, final @NonNull T provider) {
    providers.put(clazz, provider);
  }

}

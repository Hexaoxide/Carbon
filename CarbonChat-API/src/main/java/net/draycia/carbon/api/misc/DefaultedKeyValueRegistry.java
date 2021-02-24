package net.draycia.carbon.api.misc;

import net.kyori.registry.DefaultedRegistryGetter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An extension of the DefaultedRegistryGetter that declares a defaultValue method
 * @param <K> The key type
 * @param <V> The value type
 */
public interface DefaultedKeyValueRegistry<K, V> extends DefaultedRegistryGetter<K, V> {

  /**
   * Gets the default value.
   *
   * @return the default value
   */
  @NonNull V defaultValue();

}

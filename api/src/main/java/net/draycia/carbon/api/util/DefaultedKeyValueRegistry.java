package net.draycia.carbon.api.util;

import net.kyori.registry.DefaultedRegistryGetter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An extension of the DefaultedRegistryGetter that declares a defaultValue method.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @since 1.0.0
 */
public interface DefaultedKeyValueRegistry<K, V> extends DefaultedRegistryGetter<K, V> {

    /**
     * Gets the default value.
     *
     * @return the default value
     * @since 1.0.0
     */
    @NonNull V defaultValue();

}

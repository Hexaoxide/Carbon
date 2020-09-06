package net.draycia.carbon.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

public interface Registry<T> {

    /**
     * Adds the specified key and value to the registry
     *
     * @param key   The entry's key
     * @param value The value to insert
     * @return If successfully registered
     */
    boolean register(@NonNull String key, @NonNull T value);

    /**
     * Gets the list of registered values
     *
     * @return The list of registered values
     */
    @NonNull
    Collection<T> values();

    /**
     * Obtains an entry associated with the specified key
     *
     * @param key The key for the entry
     * @return The entry associated with the key
     */
    @Nullable @Nullable
    T get(@NonNull String key);

    /**
     * Clears all registered entries from the registry
     */
    void clearAll();

}

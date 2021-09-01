package net.draycia.carbon.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Static accessor for the {@link CarbonChat} class.
 *
 * @since 1.0.0
 */
@DefaultQualifier(NonNull.class)
public final class CarbonChatProvider {

    private static @Nullable CarbonChat instance;

    private CarbonChatProvider() {

    }

    /**
     * Registers the {@link CarbonChat} implementation.
     *
     * @param carbonChat the carbon implementation
     * @since 1.0.0
     */
    public static void register(final CarbonChat carbonChat) {
        CarbonChatProvider.instance = carbonChat;
    }

    /**
     * Gets the currently registered {@link CarbonChat} implementation.
     *
     * @return the registered carbon implementation
     * @since 1.0.0
     */
    public static CarbonChat carbonChat() {
        if (CarbonChatProvider.instance == null) {
            throw new IllegalStateException("CarbonChat not initialized!"); // LuckPerms design go brrrr
        }

        return CarbonChatProvider.instance;
    }

}

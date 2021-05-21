package net.draycia.carbon.api.util;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * A chat renderer that's identifiable by key.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface KeyedRenderer extends Keyed, ChatComponentRenderer {

    /**
     * Creates a new renderer with the corresponding key.
     *
     * @param key the renderer's key
     * @param renderer the chat renderer
     * @return the keyed renderer
     * @since 2.0.0
     */
    static KeyedRenderer keyedRenderer(final Key key, ChatComponentRenderer renderer) {
        return new Impl(key, renderer);
    }

    /**
     * Implementation of the keyed renderer.
     *
     * @since 2.0.0
     */
    record Impl(@NonNull Key key, ChatComponentRenderer renderer) implements KeyedRenderer {

        @Override
        public @Nullable
        Component render(
                final CarbonPlayer sender,
                final Audience recipient,
                final Component message,
                final Component originalMessage
        ) {
            return this.renderer.render(sender, recipient, message, originalMessage);
        }

    }

}

package net.draycia.carbon.api.util;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Renderer used to construct chat components on a per-player basis.
 *
 * @since 2.0.0
 */
@FunctionalInterface
@DefaultQualifier(NonNull.class)
public interface ChatComponentRenderer {

    /**
     * Renders a Component for the specified recipient.
     *
     * @param sender          the player that sent the message
     * @param recipient       a recipient of the message.
     *                        may be a player, console, or other Audience implementations
     * @param message         the message being sent
     * @param originalMessage the original message that was sent
     * @return                the component to be shown to the recipient,
     *                        or empty if the recipient should not receive the message
     * @since 2.0.0
     */
    @NonNull
    Component render(final CarbonPlayer sender,
                     final Audience recipient,
                     final Component message,
                     final Component originalMessage);

}

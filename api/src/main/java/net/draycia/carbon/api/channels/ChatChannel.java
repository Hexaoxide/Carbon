package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.ChatComponentRenderer;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.empty;

/**
 * ChatChannel interface, supplies a formatter and filters recipients.<br>
 * Extends Keyed for identification purposes.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface ChatChannel extends Keyed {

    /**
     * This channel's renderer.
     *
     * @return the renderer
     * @since 2.0.0
     */
    ChatComponentRenderer renderer();

    /**
     * Checks if the player may send messages in this channel.
     *
     * @param carbonPlayer the player attempting to speak
     * @return if the player may speak
     * @since 2.0.0
     */
    ChannelPermissionResult speechPermitted(final CarbonPlayer carbonPlayer);

    /**
     * Checks if the player may receive messages from this channel.
     *
     * @param carbonPlayer the player that's receiving messages
     * @return if the player may receive messages
     * @since 2.0.0
     */
    ChannelPermissionResult hearingPermitted(final CarbonPlayer carbonPlayer);

    /**
     * Represents the result of a channel permission check.
     */
    record ChannelPermissionResult(boolean permitted, Component reason) {

        private static final ChannelPermissionResult ALLOWED =
            new ChannelPermissionResult(true, empty());

        /**
         * Returns a result denoting that the player is permitted for the action.
         *
         * @return that the action is allowed
         * @since 2.0.0
         */
        public static ChannelPermissionResult allowed() {
            return ALLOWED;
        }

        /**
         * Returns a result denoting that the player is denied for the action.
         *
         * @return that the action is denied
         * @since 2.0.0
         */
        public static ChannelPermissionResult denied(final Component reason) {
            return new ChannelPermissionResult(false, reason);
        }

    }

}

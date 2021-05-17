package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.ChatComponentRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * ChatChannel interface, supplies a formatter and filters recipients.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface ChatChannel {

    /**
     * Checks if the player may receive messages from this channel.
     *
     * @param player the player
     * @return if the player may receive this channel's messages
     * @since 2.0.0
     */
    boolean mayReceiveMessages(final CarbonPlayer player);

    /**
     * Checks if the player may send messages in this channel.
     *
     * @param player the player
     * @return if the player may send messages in this channel
     * @since 2.0.0
     */
    boolean maySendMessages(final CarbonPlayer player);

    /**
     * This channel's renderer.
     *
     * @return the renderer
     * @since 2.0.0
     */
    ChatComponentRenderer renderer();

}

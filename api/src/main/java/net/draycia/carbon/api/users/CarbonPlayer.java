package net.draycia.carbon.api.users;

import net.draycia.carbon.api.channels.ChatChannel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.UUID;

/**
 * Generic abstraction for players.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface CarbonPlayer extends Audience, Identified {

    /**
     * Gets the player's username.
     *
     * @return the player's username
     * @since 2.0.0
     */
    String username();

    /**
     * Gets the player's display name, shown in places like chat and tab menu.
     *
     * @return the player's display name
     * @since 2.0.0
     */
    Component displayName();

    /**
     * Sets the player's display name.<br>
     * Setting null is equivalent to setting the display name to the username.
     *
     * @param displayName the new display name
     * @since 2.0.0
     */
    void displayName(final @Nullable Component displayName);

    /**
     * The player's UUID, often used for identification purposes.
     *
     * @return the player's UUID
     * @since 2.0.0
     */
    UUID uuid();

    /**
     * Creates a {@link Component} with a content and item hover given the player's actively held item.
     *
     * @return the player's held item as an item hover component
     * @since 2.0.0
     */
    Component createItemHoverComponent();

    /**
     * The player's selected channel. If none has been set, returns the server default.
     *
     * @return the player's selected channel
     * @since 2.0.0
     */
    ChatChannel selectedChannel();

    /**
     * Sets the player's selected channel.
     *
     * @param chatChannel the channel
     * @since 2.0.0
     */
    void selectedChannel(final ChatChannel chatChannel);

    boolean hasPermission(final String permission);

}

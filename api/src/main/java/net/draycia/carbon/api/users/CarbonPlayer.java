package net.draycia.carbon.api.users;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.punishments.MuteEntry;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

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
     * Checks if the player has a display name set through {@link CarbonPlayer#displayName(Component)}
     *
     * @return if the player has a display name set through {@link CarbonPlayer#displayName(Component)}
     */
    boolean hasCustomDisplayName();

    /**
     * Gets the player's display name, shown in places like chat and tab menu.
     *
     * @return the player's display name
     * @since 2.0.0
     */
    @Nullable Component displayName();

    /**
     * Sets the player's display name.<br>
     * Setting null is equivalent to setting the display name to the username.
     *
     * @param displayName the new display name
     * @since 2.0.0
     */
    void displayName(final @Nullable Component displayName);

    /**
     * Temporarily sets the player's display name.<br>
     * Setting null is equivalent to setting the display name to the username.
     * If the player logs off or is otherwise disconnected from the server,
     *  their temporary display name will be reset.
     *
     * @param temporaryDisplayName the new display name
     * @param expirationEpoch when the display name expires, in milliseconds from Unix epoch, or -1 for no expiration
     * @since 2.0.0
     */
    void temporaryDisplayName(final @Nullable Component temporaryDisplayName, final long expirationEpoch);

    /**
     * The player's temporary display name.
     *
     * @return the player's temporary display name.
     */
    // TODO: improve javadocs here
    @Nullable Component temporaryDisplayName();

    /**
     * The expiration date in milliseconds for the temporary display name.
     *
     * @return the temporary display name's expiration in MS since Unix Epoch
     */
    long temporaryDisplayNameExpiration();

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
     * The player's locale.
     *
     * @return the player's locale, or null if offline
     * @since 2.0.0
     */
    @Nullable Locale locale();

    /**
     * The player's selected channel, or null if one isn't set.
     *
     * @return the player's selected channel
     * @since 2.0.0
     */
    @Nullable ChatChannel selectedChannel();

    /**
     * Sets the player's selected channel.
     *
     * @param chatChannel the channel
     * @since 2.0.0
     */
    void selectedChannel(final @Nullable ChatChannel chatChannel);

    /**
     * Checks if the player has the specified permission.
     *
     * @param permission the permission to check
     * @return if the player has the permission
     * @since 2.0.0
     */
    boolean hasPermission(final String permission);

    /**
     * Returns the player's primary group.
     *
     * @return the player's primary group
     * @since 2.0.0
     */
    String primaryGroup();

    /**
     * Returns the complete list of groups the player is in.
     *
     * @return the groups the player is in
     * @since 2.0.0
     */
    List<String> groups();

    /**
     * A copy of the mute entries the player has.
     *
     * @return a copy of the player's mute entries
     * @since 2.0.0
     */
    List<MuteEntry> muteEntries();

    /**
     * Returns if the player is muted for the channel and unable to speak in it.
     *
     * @param chatChannel the chat channel
     * @return if the player is muted in the channel
     * @since 2.0.0
     */
    boolean muted(final ChatChannel chatChannel);

    /**
     * Mutes and unmutes the player for the specified channel.
     *
     * @param chatChannel the channel to mute/unmute for
     * @param muted if the player should be muted for the channel
     * @param cause the UUID of the cause of the mute, typically a player UUID
     * @param duration the duration of the mute, or -1 if it does not expire
     * @param reason the reason the player was muted
     * @since 2.0.0
     */
    void addMuteEntry(
        final @Nullable ChatChannel chatChannel,
        final boolean muted,
        final @Nullable UUID cause,
        final long duration,
        final @Nullable String reason
    );

    /**
     * Returns if the player is deafened and unable to read messages.
     *
     * @return if the player is deafened
     * @since 2.0.0
     */
    boolean deafened();

    /**
     * Deafens and undeafens the player.
     *
     * @since 2.0.0
     */
    void deafened(final boolean deafened);

    /**
     * Returns if the player is spying on messages and able to read muted/private messages.
     *
     * @return if the player is spying on messages
     * @since 2.0.0
     */
    boolean spying();

    /**
     * Sets and unsets the player's ability to spy.
     *
     * @since 2.0.0
     */
    void spying(final boolean spying);

    /**
     * Sends the message as the player.
     *
     * @param message the message to be sent
     * //@return the message result
     * @since 2.0.0
     */
    // TODO: change return type, provide information useful like what message was actually sent?
    void sendMessageAsPlayer(final String message);

    /**
     * Returns whether or not the player is online.
     *
     * @return if the player is online.
     * @since 2.0.0
     */
    boolean online();

    /**
     * The UUID of the player that replies will be sent to.
     *
     * @return the player's reply target
     * @since 2.0.0
     */
    @Nullable UUID whisperReplyTarget();

    /**
     * Sets the whisper reply target for this player.
     *
     * @param uuid the uuid of the reply target
     * @since 2.0.0
     */
    void whisperReplyTarget(final @Nullable UUID uuid);

    /**
     * The last player this player has whispered.
     *
     * @return the player's last whisper target
     * @since 2.0.0
     */
    @Nullable UUID lastWhisperTarget();

    /**
     * Sets the last player this player has whispered.
     *
     * @param uuid the uuid of the whisper target
     * @since 2.0.0
     */
    void lastWhisperTarget(final @Nullable UUID uuid);

}

package net.draycia.carbon.channels;

import net.draycia.carbon.events.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ChatChannel implements ForwardingAudience {

    /**
     * @return The color that represents this channel. Optionally used in formatting.
     */
    public abstract @Nullable TextColor getChannelColor(ChatUser user);

    /**
     * @return The MiniMessage styled format for the group in this channel.
     */
    public abstract @Nullable String getFormat(String group);

    /**
     * @return If this is the default (typically Global) channel players use when they're in no other channel.
     */
    public abstract Boolean isDefault();

    /**
     * @return If this channel can be toggled off and if players can ignore player messages in this channel.
     */
    public abstract Boolean isIgnorable();

    /**
     *
     * @return If this channel should be synced cross server
     *
     * @deprecated Use {@link #isCrossServer()} instead
     */
    @Deprecated
    public abstract Boolean shouldBungee();

    /**
     * @return If this channel should be synced cross server
     */
    public abstract Boolean isCrossServer();

    /**
     * @return The name of this channel.
     */
    public abstract String getName();

    public abstract String getKey();

    @Nullable
    public abstract @Nullable String getMessagePrefix();

    public abstract @Nullable String getAliases();

    /**
     * @return The message to be sent to the player when switching to this channel.
     */
    public abstract @Nullable String getSwitchMessage();

    public abstract @Nullable String getSwitchOtherMessage();

    public abstract @Nullable String getSwitchFailureMessage();

    public abstract @Nullable String getCannotIgnoreMessage();

    /**
     * @return The message to be send to the player when toggling this channel off.
     */

    public abstract @Nullable String getToggleOffMessage();

    /**
     * @return The message to be send to the player when toggling this channel on.
     */

    public abstract @Nullable String getToggleOnMessage();

    public abstract @Nullable String getToggleOtherOnMessage();

    public abstract @Nullable String getToggleOtherOffMessage();

    public abstract @Nullable String getCannotUseMessage();

    public abstract Boolean primaryGroupOnly();

    public abstract Boolean honorsRecipientList();

    public abstract Boolean permissionGroupMatching();

    public abstract boolean testContext(ChatUser sender, ChatUser target);

    public abstract @Nullable Object getContext(String key);

    public abstract @NonNull List<String> getGroupOverrides();

    /**
     * @return If the player can use this channel.
     */
    public abstract Boolean canPlayerUse(ChatUser user);

    public abstract @NonNull Boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying);

    public abstract @NonNull Boolean canPlayerSee(ChatUser target, boolean checkSpying);

    /**
     * @return If the channel should forward its formatting / formatted message to other servers
     */
    public Boolean shouldForwardFormatting() {
        return true;
    }

    public abstract @NonNull List<Pattern> getItemLinkPatterns();

    /**
     * Parses the specified message, calls a {@link PreChatFormatEvent}, and sends the message to everyone who can view this channel.
     * @param user The player who is saying the message.
     * @param message The message to be sent.
     */
    public abstract Component sendMessage(ChatUser user, String message, boolean fromBungee);

    public abstract Component sendMessage(ChatUser user, Collection<ChatUser> recipients, String message, boolean fromBungee);

    public abstract void sendComponent(ChatUser user, Component component);

    public String processPlaceholders(ChatUser user, String input) { return input; }

    public abstract boolean shouldCancelChatEvent();

}

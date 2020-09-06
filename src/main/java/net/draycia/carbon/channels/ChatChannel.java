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
    @Nullable
    public abstract TextColor getChannelColor(@NonNull ChatUser user);

    /**
     * @return The MiniMessage styled format for the group in this channel.
     */
    @Nullable
    public abstract String getFormat(@NonNull String group);

    /**
     * @return If this is the default (typically Global) channel players use when they're in no other channel.
     */
    public abstract boolean isDefault();

    /**
     * @return If this channel can be toggled off and if players can ignore player messages in this channel.
     */
    public abstract boolean isIgnorable();

    /**
     * @return If this channel should be synced cross server
     * @deprecated Use {@link #isCrossServer()} instead
     */
    @Deprecated
    public abstract boolean shouldBungee();

    /**
     * @return If this channel should be synced cross server
     */
    public abstract boolean isCrossServer();

    /**
     * @return The name of this channel.
     */
    @NonNull
    public abstract String getName();

    @NonNull
    public abstract String getKey();

    @Nullable
    public abstract String getMessagePrefix();

    @Nullable
    public abstract String getAliases();

    /**
     * @return The message to be sent to the player when switching to this channel.
     */
    @Nullable
    public abstract String getSwitchMessage();

    @Nullable
    public abstract String getSwitchOtherMessage();

    @Nullable
    public abstract String getSwitchFailureMessage();

    @Nullable
    public abstract String getCannotIgnoreMessage();

    /**
     * @return The message to be send to the player when toggling this channel off.
     */
    @Nullable
    public abstract String getToggleOffMessage();

    /**
     * @return The message to be send to the player when toggling this channel on.
     */
    @Nullable
    public abstract String getToggleOnMessage();

    @Nullable
    public abstract String getToggleOtherOnMessage();

    @Nullable
    public abstract String getToggleOtherOffMessage();

    @Nullable
    public abstract String getCannotUseMessage();

    public abstract boolean primaryGroupOnly();

    public abstract boolean honorsRecipientList();

    public abstract boolean permissionGroupMatching();

    public abstract boolean testContext(@NonNull ChatUser sender, @NonNull ChatUser target);

    @Nullable
    public abstract Object getContext(@NonNull String key);

    @NonNull
    public abstract List<@NonNull String> getGroupOverrides();

    /**
     * @return If the player can use this channel.
     */
    public abstract boolean canPlayerUse(@NonNull ChatUser user);

    public abstract boolean canPlayerSee(@NonNull ChatUser sender, @NonNull ChatUser target, boolean checkSpying);

    public abstract boolean canPlayerSee(@NonNull ChatUser target, boolean checkSpying);

    /**
     * @return If the channel should forward its formatting / formatted message to other servers
     */
    public boolean shouldForwardFormatting() {
        return true;
    }

    @NonNull
    public abstract List<@NonNull Pattern> getItemLinkPatterns();

    /**
     * Parses the specified message, calls a {@link PreChatFormatEvent}, and sends the message to everyone who can view this channel.
     *
     * @param user    The player who is saying the message.
     * @param message The message to be sent.
     */
    @NonNull
    public abstract Component sendMessage(@NonNull ChatUser user, @NonNull String message, boolean fromBungee);

    @NonNull
    public abstract Component sendMessage(@NonNull ChatUser user, @NonNull Collection<@NonNull ChatUser> recipients, @NonNull String message, boolean fromBungee);

    public abstract void sendComponent(@NonNull ChatUser user, @NonNull Component component);

    @Nullable
    public String processPlaceholders(@NonNull ChatUser user, @Nullable String input) { return input; }

    public abstract boolean shouldCancelChatEvent();

}

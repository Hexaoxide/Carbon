package net.draycia.carbon.channels;

import net.draycia.carbon.events.ChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

public abstract class ChatChannel {

    /**
     * @return The color that represents this channel. Optionally used in formatting.
     */
    public abstract TextColor getColor();

    /**
     * @return The MiniMessage styled format for the group in this channel.
     */
    public abstract String getFormat(String group);

    /**
     * @return If this is the default (typically Global) channel players use when they're in no other channel.
     */
    public abstract Boolean isDefault();

    /**
     * @return If this channel can be toggled off and if players can ignore player messages in this channel.
     */
    public abstract Boolean isIgnorable();

    public abstract Boolean shouldBungee();

    /**
     * @return The name of this channel.
     */
    public abstract String getName();

    public abstract String getKey();

    @Nullable
    public abstract String getMessagePrefix();

    public abstract String getAliases();

    /**
     * @return The message to be sent to the player when switching to this channel.
     */
    public abstract String getSwitchMessage();

    public abstract String getSwitchOtherMessage();

    /**
     * @return The message to be send to the player when toggling this channel off.
     */

    public abstract String getToggleOffMessage();

    /**
     * @return The message to be send to the player when toggling this channel on.
     */

    public abstract String getToggleOnMessage();

    public abstract String getToggleOtherOnMessage();

    public abstract String getToggleOtherOffMessage();

    public abstract String getCannotUseMessage();

    public abstract Boolean primaryGroupOnly();

    public abstract boolean testContext(ChatUser sender, ChatUser target);

    public abstract Object getContext(String key);

    /**
     * @return If the player can use this channel.
     */
    public abstract Boolean canPlayerUse(ChatUser user);

    public abstract Boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying);

    public abstract List<ChatUser> getAudience(ChatUser user);

    /**
     * @return If the channel should forward its formatting / formatted message to other servers
     */
    public Boolean shouldForwardFormatting() {
        return true;
    }

    public abstract List<Pattern> getItemLinkPatterns();

    /**
     * Parses the specified message, calls a {@link ChatFormatEvent}, and sends the message to everyone who can view this channel.
     * @param user The player who is saying the message.
     * @param message The message to be sent.
     */
    public abstract void sendMessage(ChatUser user, String message, boolean fromBungee);

    public abstract void sendComponent(ChatUser user, Component component);

    public String processPlaceholders(ChatUser user, String input) { return input; }

}

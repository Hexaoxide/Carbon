package net.draycia.simplechat.channels;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.events.ChatFormatEvent;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Map;
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
    public abstract boolean isDefault();

    /**
     * @return If this channel can be toggled off and if players can ignore player messages in this channel.
     */
    public abstract boolean isIgnorable();

    public abstract boolean shouldBungee();

    /**
     * @return The name of this channel.
     */
    public abstract String getName();

    /**
     * @return The distance other players must be within to the sender to see messages in this channel.
     */
    public abstract double getDistance();

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

    public abstract boolean filterEnabled();

    /**
     * @return If this channel is Towny's town chat.
     */
    public boolean isTownChat() {
        return false;
    }

    /**
     * @return If this channel is Towny's nation chat.
     */
    public boolean isNationChat() {
        return false;
    }

    /**
     * @return If this channel is a Towny alliance chat.
     */
    public boolean isAllianceChat() {
        return false;
    }

    /**
     * @return If this channel is mcMMO's party chat.
     */
    public boolean isPartyChat() {
        return false;
    }

    public abstract boolean firstMatchingGroup();

    /**
     * @return If the player can use this channel.
     */
    public abstract boolean canPlayerUse(ChatUser user);

    public abstract boolean canPlayerSee(ChatUser sender, ChatUser target, boolean checkSpying);

    public abstract List<ChatUser> getAudience(ChatUser user);

    /**
     * @return If the channel should forward its formatting / formatted message to other servers
     */
    public boolean shouldForwardFormatting() {
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

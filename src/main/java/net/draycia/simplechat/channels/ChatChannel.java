package net.draycia.simplechat.channels;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;
import java.util.Map;

public abstract class ChatChannel {

    /**
     * @return The color that represents this channel. Optionally used in formatting.
     */
    public abstract TextColor getColor();

    /**
     * @return The ID of the discord channel to read messages from for chat bridging.
     */
    public abstract long getChannelId();

    /**
     * @return The MiniMessage styled format for the group in this channel.
     */
    public abstract String getFormat(String group);

    /**
     * @return The url of the webhook used to send messages to discord for chat bridging.
     */
    public abstract String getWebhook();

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

    /**
     * @return The message to be send to the player when toggling this channel off.
     */

    public abstract String getToggleOffMessage();

    /**
     * @return The message to be send to the player when toggling this channel on.
     */

    public abstract String getToggleOnMessage();

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

    /**
     * @return If the player can use this channel.
     */
    public abstract boolean canPlayerUse(ChatUser user);

    public abstract boolean canPlayerSee(ChatUser sender, ChatUser target);

    public abstract List<Player> getAudience(ChatUser user);

    /**
     * @return If the channel should forward its formatting / formatted message to other servers
     */
    public boolean shouldForwardFormatting() {
        return true;
    }

    /**
     * Parses the specified message, calls a {@link net.draycia.simplechat.events.ChannelChatEvent}, and sends the message to everyone who can view this channel.
     * @param user The player who is saying the message.
     * @param message The message to be sent.
     */
    public abstract void sendMessage(ChatUser user, String message, boolean fromBungee);

    public abstract void sendComponent(ChatUser user, Component component);

    public abstract void processDiscordMessage(MessageCreateEvent event);

    public static abstract class Builder {
        public abstract ChatChannel build(SimpleChat simpleChat);
        public abstract ChatChannel.Builder setColor(TextColor color);
        public abstract ChatChannel.Builder setColor(String color);
        public abstract ChatChannel.Builder setId(long id);
        public abstract ChatChannel.Builder setFormats(Map<String, String> formats);
        public abstract ChatChannel.Builder setWebhook(String webhook);
        public abstract ChatChannel.Builder setIsDefault(boolean aDefault);
        public abstract ChatChannel.Builder setIgnorable(boolean ignorable);
        public abstract ChatChannel.Builder setName(String name);
        public abstract ChatChannel.Builder setDistance(double distance);
        public abstract ChatChannel.Builder setSwitchMessage(String switchMessage);
        public abstract ChatChannel.Builder setToggleOffMessage(String toggleOffMessage);
        public abstract ChatChannel.Builder setToggleOnMessage(String toggleOnMessage);
        public abstract ChatChannel.Builder setShouldForwardFormatting(boolean forwardFormatting);
        public abstract ChatChannel.Builder setShouldBungee(boolean shouldBungee);
        public abstract ChatChannel.Builder setFilterEnabled(boolean filterEnabled);
    }

}

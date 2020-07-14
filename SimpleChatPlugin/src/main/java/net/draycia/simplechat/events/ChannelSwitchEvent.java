package net.draycia.simplechat.events;

import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChannelSwitchEvent extends Event {

    /** Bukkit event stuff **/
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /** Relevant stuff **/
    private ChatChannel channel;
    private ChatUser user;

    public ChannelSwitchEvent(ChatChannel channel, ChatUser user) {
        super(!Bukkit.isPrimaryThread());

        this.channel = channel;
        this.user = user;
    }

    public ChatUser getUser() {
        return user;
    }

    public ChatChannel getChannel() {
        return channel;
    }

}

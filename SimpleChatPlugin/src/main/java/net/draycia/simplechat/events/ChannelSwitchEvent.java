package net.draycia.simplechat.events;

import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ChannelSwitchEvent extends Event implements Cancellable {

    /** Bukkit event stuff **/
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /** Relevant stuff **/
    private ChatChannel channel;
    private ChatUser user;
    private String failureMessage;

    public ChannelSwitchEvent(ChatChannel channel, ChatUser user, String failureMessage) {
        super(!Bukkit.isPrimaryThread());

        this.channel = channel;
        this.user = user;
        this.failureMessage = failureMessage;
    }

    public ChatUser getUser() {
        return user;
    }

    public ChatChannel getChannel() {
        return channel;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
}

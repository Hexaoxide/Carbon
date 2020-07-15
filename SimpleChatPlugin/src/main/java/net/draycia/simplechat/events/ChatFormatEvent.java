package net.draycia.simplechat.events;

import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ChatFormatEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled = false;

    private ChatUser user;
    private ChatChannel chatChannel;
    private String format;
    private String message;

    public ChatFormatEvent(ChatUser user, ChatChannel chatChannel, String format, String message) {
        super(!Bukkit.isPrimaryThread());

        this.user = user;
        this.chatChannel = chatChannel;
        this.format = format;
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public ChatUser getUser() {
        return user;
    }

    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    public void setChatChannel(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

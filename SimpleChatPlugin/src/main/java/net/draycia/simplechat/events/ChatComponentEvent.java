package net.draycia.simplechat.events;

import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatComponentEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled = false;

    private ChatUser user;
    private ChatChannel chatChannel;
    private TextComponent component;
    private String originalMessage;
    private List<ChatUser> recipients;
    private boolean customPlayerFormat;

    public ChatComponentEvent(ChatUser user, ChatChannel chatChannel, TextComponent component, String originalMessage, List<ChatUser> recipients) {
        this(user, chatChannel, component, originalMessage, recipients, false);
    }

    public ChatComponentEvent(ChatUser user, ChatChannel chatChannel, TextComponent component, String originalMessage, List<ChatUser> recipients, boolean customPlayerFormat) {
        super(true);

        this.user = user;
        this.chatChannel = chatChannel;
        this.component = component;
        this.originalMessage = originalMessage;
        this.recipients = recipients;
        this.customPlayerFormat = customPlayerFormat;
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

    public TextComponent getComponent() {
        return component;
    }

    public void setComponent(TextComponent component) {
        this.component = component;
    }

    public List<ChatUser> getRecipients() {
        return recipients;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public boolean isCustomPlayerFormat() {
        return customPlayerFormat;
    }
}

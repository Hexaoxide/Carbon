package net.draycia.carbon.events;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatComponentEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled = false;

    private @NotNull final ChatUser sender;
    private @Nullable final ChatUser target;
    private ChatChannel chatChannel;
    private Component component;
    private final String originalMessage;

    public ChatComponentEvent(@NotNull ChatUser sender, @Nullable ChatUser target, ChatChannel chatChannel, Component component, String originalMessage) {
        super(!Bukkit.isPrimaryThread());

        this.sender = sender;
        this.target = target;
        this.chatChannel = chatChannel;
        this.component = component;
        this.originalMessage = originalMessage;
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

    @NotNull
    public ChatUser getSender() {
        return sender;
    }

    @Nullable
    public ChatUser getTarget() {
        return target;
    }

    public ChatChannel getChannel() {
        return chatChannel;
    }

    public void setChannel(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

}

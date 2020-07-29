package net.draycia.carbon.events;

import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PrivateMessageEvent extends Event {

    /** Bukkit event stuff **/
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /** Relevant stuff **/
    private final ChatUser sender;
    private final ChatUser target;
    private final Component senderComponent;
    private final Component targetComponent;
    private final String message;

    public PrivateMessageEvent(ChatUser sender, ChatUser target, Component senderComponent, Component targetComponent, String message) {
        super(!Bukkit.isPrimaryThread());

        this.sender = sender;
        this.target = target;
        this.senderComponent = senderComponent;
        this.targetComponent = targetComponent;
        this.message = message;
    }

    public ChatUser getSender() {
        return sender;
    }

    public ChatUser getTarget() {
        return target;
    }

    public Component getSenderComponent() {
        return senderComponent;
    }

    public Component getTargetComponent() {
        return targetComponent;
    }

    public String getMessage() {
        return message;
    }

}

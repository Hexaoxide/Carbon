package net.draycia.carbon.events;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.util.Registry;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChannelRegisterEvent extends Event {

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
    private List<ChatChannel> registeredChannels;
    private Registry<ChatChannel> registry;

    public ChannelRegisterEvent(List<ChatChannel> registeredChannels, Registry<ChatChannel> registry) {
        super(!Bukkit.isPrimaryThread());

        this.registeredChannels = registeredChannels;
        this.registry = registry;
    }

    public void register(ChatChannel chatChannel) {
        registry.register(chatChannel.getKey(), chatChannel);
    }

    public List<ChatChannel> getRegisteredChannels() {
        return registeredChannels;
    }
}

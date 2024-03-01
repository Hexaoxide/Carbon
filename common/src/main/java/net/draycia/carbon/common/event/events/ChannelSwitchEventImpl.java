package net.draycia.carbon.common.event.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.events.ChannelSwitchEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ChannelSwitchEventImpl implements ChannelSwitchEvent {

    private final CarbonPlayer player;
    private ChatChannel chatChannel;
    private boolean cancelled = false;

    public ChannelSwitchEventImpl(final CarbonPlayer player, final ChatChannel chatChannel) {
        this.player = player;
        this.chatChannel = chatChannel;
    }

    @Override
    public boolean cancelled() {
        return this.cancelled;
    }

    @Override
    public void cancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public CarbonPlayer player() {
        return this.player;
    }

    @Override
    public ChatChannel channel() {
        return this.chatChannel;
    }

    @Override
    public void channel(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

}

package net.draycia.simplechat.managers.luckperms.contexts;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;

public class CanSeeChannelCalculator implements ContextCalculator<Player> {

    private static final String KEY = "can-see-channel";

    private SimpleChat simpleChat;

    public CanSeeChannelCalculator(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Override
    public void calculate(Player player, ContextConsumer consumer) {
        for (ChatChannel channel : simpleChat.getChannelManager().getRegistry().values()) {
            boolean canUse = channel.canPlayerUse(simpleChat.getUserService().wrap(player));

            consumer.accept(KEY + ":" + channel.getKey(), canUse ? "true" : "false");
        }
    }

    @Override
    public net.luckperms.api.context.ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();

        for (ChatChannel channel : simpleChat.getChannelManager().getRegistry().values()) {
            builder.add(KEY + ":" + channel.getKey(), "true");
            builder.add(KEY + ":" + channel.getKey(), "false");
        }

        return builder.build();
    }

}

package net.draycia.carbon.common.channels;

import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.kyori.adventure.key.Key;
import net.kyori.registry.DefaultedRegistryImpl;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CarbonChannelRegistry extends DefaultedRegistryImpl<Key, ChatChannel> implements ChannelRegistry {

    private final @NonNull ChatChannel defaultChannel;

    public CarbonChannelRegistry(final @NonNull ChatChannel defaultChannel) {
        super(defaultChannel.key());
        this.defaultChannel = defaultChannel;
    }

    @Override
    public @NonNull ChatChannel defaultValue() {
        return this.defaultChannel;
    }

}

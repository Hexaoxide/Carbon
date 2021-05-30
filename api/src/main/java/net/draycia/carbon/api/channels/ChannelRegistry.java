package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.util.DefaultedKeyValueRegistry;
import net.kyori.adventure.key.Key;

public interface ChannelRegistry extends DefaultedKeyValueRegistry<Key, ChatChannel> {
}

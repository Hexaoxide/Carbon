package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.util.DefaultedKeyValueRegistry;
import net.kyori.adventure.key.Key;

/**
 * Registry for chat channels.
 *
 * @since 2.0.0
 */
public interface ChannelRegistry extends DefaultedKeyValueRegistry<Key, ChatChannel> {
}

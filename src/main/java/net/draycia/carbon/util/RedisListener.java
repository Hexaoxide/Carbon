package net.draycia.carbon.util;

import io.lettuce.core.pubsub.RedisPubSubListener;

@FunctionalInterface
public interface RedisListener extends RedisPubSubListener<String, String> {

    void message(String channel, String message);

    @Override
    default void message(String pattern, String channel, String message) { }

    @Override
    default void subscribed(String channel, long count) { }

    @Override
    default void psubscribed(String pattern, long count) { }

    @Override
    default void unsubscribed(String channel, long count) { }

    @Override
    default void punsubscribed(String pattern, long count) { }

}

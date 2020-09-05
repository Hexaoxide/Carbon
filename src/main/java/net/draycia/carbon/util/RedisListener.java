package net.draycia.carbon.util;

import io.lettuce.core.pubsub.RedisPubSubListener;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface RedisListener extends RedisPubSubListener<String, String> {

    @Override
    void message(@NotNull String channel, @NotNull String message);

    @Override
    default void message(@NotNull String pattern, @NotNull String channel, @NotNull String message) { }

    @Override
    default void subscribed(@NotNull String channel, long count) { }

    @Override
    default void psubscribed(@NotNull String pattern, long count) { }

    @Override
    default void unsubscribed(@NotNull String channel, long count) { }

    @Override
    default void punsubscribed(@NotNull String pattern, long count) { }

}

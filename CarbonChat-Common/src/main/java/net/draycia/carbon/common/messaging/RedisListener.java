package net.draycia.carbon.common.messaging;

import io.lettuce.core.pubsub.RedisPubSubListener;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface RedisListener extends RedisPubSubListener<String, String> {

  @Override
  void message(@NonNull String channel, @NonNull String message);

  @Override
  default void message(final @NonNull String pattern, final @NonNull String channel, final @NonNull String message) {
  }

  @Override
  default void subscribed(final @NonNull String channel, final long count) {
  }

  @Override
  default void psubscribed(final @NonNull String pattern, final long count) {
  }

  @Override
  default void unsubscribed(final @NonNull String channel, final long count) {
  }

  @Override
  default void punsubscribed(final @NonNull String pattern, final long count) {
  }

}

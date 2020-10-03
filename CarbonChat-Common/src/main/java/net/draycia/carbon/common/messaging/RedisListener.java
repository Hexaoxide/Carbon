package net.draycia.carbon.common.messaging;

import io.lettuce.core.pubsub.RedisPubSubListener;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface RedisListener extends RedisPubSubListener<String, String> {

  @Override
  void message(@NonNull String channel, @NonNull String message);

  @Override
  default void message(@NonNull final String pattern, @NonNull final String channel, @NonNull final String message) {
  }

  @Override
  default void subscribed(@NonNull final String channel, final long count) {
  }

  @Override
  default void psubscribed(@NonNull final String pattern, final long count) {
  }

  @Override
  default void unsubscribed(@NonNull final String channel, final long count) {
  }

  @Override
  default void punsubscribed(@NonNull final String pattern, final long count) {
  }

}

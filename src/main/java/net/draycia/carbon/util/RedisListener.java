package net.draycia.carbon.util;

import io.lettuce.core.pubsub.RedisPubSubListener;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface RedisListener extends RedisPubSubListener<String, String> {

  @Override
  void message(@NonNull String channel, @NonNull String message);

  @Override
  default void message(@NonNull String pattern, @NonNull String channel, @NonNull String message) {
  }

  @Override
  default void subscribed(@NonNull String channel, long count) {
  }

  @Override
  default void psubscribed(@NonNull String pattern, long count) {
  }

  @Override
  default void unsubscribed(@NonNull String channel, long count) {
  }

  @Override
  default void punsubscribed(@NonNull String pattern, long count) {
  }

}

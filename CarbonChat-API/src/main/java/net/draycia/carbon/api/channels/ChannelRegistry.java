package net.draycia.carbon.api.channels;

import net.kyori.registry.DefaultedRegistry;
import net.kyori.registry.Registry;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChannelRegistry implements Registry<String, ChatChannel>, DefaultedRegistry<String, ChatChannel> {

  private @NonNull final Map<@NonNull String, @NonNull ChatChannel> registry = new HashMap<>();

  private @MonotonicNonNull ChatChannel defaultChannel = null;

  @Override
  public @NonNull ChatChannel register(@NonNull final String key, @NonNull final ChatChannel value) {
    this.registry.putIfAbsent(key, value);

    if (value instanceof TextChannel) {
      if (this.defaultChannel == null && ((TextChannel) value).isDefault()) {
        this.defaultChannel = value;
      }
    }

    return value;
  }

  @Override
  public @Nullable ChatChannel get(@NonNull final String key) {
    return this.registry.get(key);
  }

  @Override
  public @Nullable String key(@NonNull final ChatChannel value) {
    return null;
  }

  @Override
  public @NonNull Set<String> keySet() {
    return this.registry.keySet();
  }

  @Override
  public @NonNull Iterator<ChatChannel> iterator() {
    return this.registry.values().iterator();
  }

  public @MonotonicNonNull ChatChannel defaultChannel() {
    return this.defaultChannel;
  }

  @Override
  public @NonNull String defaultKey() {
    return this.defaultChannel.key();
  }

  @Override
  public @NonNull ChatChannel getOrDefault(@NonNull final String key) {
    return this.registry.getOrDefault(key, this.defaultChannel());
  }
}

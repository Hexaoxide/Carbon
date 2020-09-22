package net.draycia.carbon.api.channels;

import net.kyori.registry.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChannelRegistry implements Registry<String, ChatChannel> {

  @NonNull
  private final Map<@NonNull String, @NonNull ChatChannel> registry = new HashMap<>();

  @Nullable
  private ChatChannel defaultChannel = null;

  @Override
  public @NonNull ChatChannel register(final @NonNull String key, final @NonNull ChatChannel value) {
    this.registry.putIfAbsent(key, value);

    if (this.defaultChannel == null && value.isDefault()) {
      this.defaultChannel = value;
    }

    return value;
  }

  @Override
  public @Nullable ChatChannel get(final @NonNull String key) {
    return this.registry.get(key);
  }

  public @Nullable ChatChannel channelOrDefault(final @NonNull String key) {
    return this.registry.getOrDefault(key, this.defaultChannel());
  }

  @Override
  public @Nullable String key(final @NonNull ChatChannel value) {
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

  public @Nullable ChatChannel defaultChannel() {
    return this.defaultChannel;
  }

}

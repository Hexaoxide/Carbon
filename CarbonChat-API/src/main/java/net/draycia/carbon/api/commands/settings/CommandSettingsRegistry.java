package net.draycia.carbon.api.commands.settings;

import net.draycia.carbon.api.channels.ChatChannel;
import net.kyori.registry.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@ConfigSerializable
public class CommandSettingsRegistry implements Registry<String, CommandSettings> {

  @Setting
  private final @NonNull Map<@NonNull String, @NonNull CommandSettings> registry = new HashMap<>();

  private static final ObjectMapper<CommandSettingsRegistry> MAPPER;

  static {
    try {
      MAPPER = ObjectMapper.factory().get(CommandSettingsRegistry.class);
    } catch (final SerializationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static CommandSettingsRegistry loadFrom(final CommentedConfigurationNode node) throws SerializationException {
    return MAPPER.load(node);
  }

  public void saveTo(final CommentedConfigurationNode node) throws SerializationException {
    MAPPER.save(this, node);
  }

  @Override
  public @NonNull CommandSettings register(final @NonNull String key, final @NonNull CommandSettings value) {
    this.registry.putIfAbsent(key, value);

    return value;
  }

  @Override
  public @Nullable CommandSettings get(final @NonNull String key) {
    return this.registry.get(key);
  }

  public @Nullable CommandSettings get(final @NonNull ChatChannel channel) {
    return this.get(channel.key());
  }

  @Override
  public @Nullable String key(final @NonNull CommandSettings value) {
    return null;
  }

  @Override
  public @NonNull Set<String> keySet() {
    return (Set<String>) this.registry.keySet(); // https://github.com/typetools/checker-framework/issues/3638
  }

  @Override
  public @NonNull Iterator<CommandSettings> iterator() {
    return this.registry.values().iterator();
  }

}

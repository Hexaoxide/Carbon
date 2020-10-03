package net.draycia.carbon.api.commands.settings;

import net.draycia.carbon.api.channels.ChatChannel;
import net.kyori.registry.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@ConfigSerializable
public class CommandSettingsRegistry implements Registry<String, CommandSettings> {

  private static final ObjectMapper<CommandSettingsRegistry> MAPPER;

  static {
    try {
      MAPPER = ObjectMapper.forClass(CommandSettingsRegistry.class); // We hold on to the instance of our ObjectMapper
    } catch (final ObjectMappingException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static CommandSettingsRegistry loadFrom(final CommentedConfigurationNode node) throws ObjectMappingException {
    return MAPPER.bindToNew().populate(node);
  }

  public void saveTo(final CommentedConfigurationNode node) throws ObjectMappingException {
    MAPPER.bind(this).serialize(node);
  }

  @Setting
  private @NonNull final Map<@NonNull String, @NonNull CommandSettings> registry = new HashMap<>();

  @Override
  public @NonNull CommandSettings register(@NonNull final String key, @NonNull final CommandSettings value) {
    this.registry.put(key, value);

    return value;
  }

  @Override
  public @Nullable CommandSettings get(@NonNull final String key) {
    return this.registry.get(key);
  }

  public @Nullable CommandSettings get(@NonNull final ChatChannel channel) {
    return this.get(channel.key());
  }

  @Override
  public @Nullable String key(@NonNull final CommandSettings value) {
    return null;
  }

  @Override
  public @NonNull Set<String> keySet() {
    return this.registry.keySet();
  }

  @Override
  public @NonNull Iterator<CommandSettings> iterator() {
    return this.registry.values().iterator();
  }

}

package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class ChannelSettings {

  private static final ObjectMapper<ChannelSettings> MAPPER;

  static {
    try {
      MAPPER = ObjectMapper.factory().get(ChannelSettings.class);
    } catch (final SerializationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static ChannelSettings loadFrom(final CommentedConfigurationNode node) throws SerializationException {
    return MAPPER.load(node);
  }

  public void saveTo(final CommentedConfigurationNode node) throws SerializationException {
    MAPPER.save(this, node);
  }

  @Setting
  @Comment("All options here act like the defaults for all channels.\n" +
    "Adding any of these options to any channel overrides the relevant defaults.")
  private SharedChannelOptions sharedChannelOptions = new SharedChannelOptions();

  @Setting
  private Map<String, ChannelOptions> channelOptions =
    Collections.singletonMap("global", ChannelOptions.defaultChannel());

  @Setting
  @Comment("Used for message formats - custom tags that are replaced in descending order" +
    "\nFor example, in the default config, <group> wil be replaced with <green>%vault_group%")
  private @NonNull Map<@Nullable String, @NonNull String> customPlaceholders = new HashMap<String, String>() {
    {
      this.put("prefix", "<gray>[<group><gray>]");
      this.put("group", "<green>%vault_group%");
    }
  };

  public SharedChannelOptions defaultChannelOptions() {
    return this.sharedChannelOptions;
  }

  public Map<String, ChannelOptions> channelOptions() {
    return this.channelOptions;
  }

  public Map<String, String> customPlaceholders() {
    return this.customPlaceholders;
  }

  public ChannelOptions channelOptions(final String name) {
    return this.channelOptions.get(name);
  }

}

package net.draycia.carbon.api.config;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
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
  @Comment("#############################################\n" +
    "#                                           #\n" +
    "#      Shared/Default Channel Options       #\n" +
    "#                                           #\n" +
    "#############################################\n" +
    "\n" +
    "# All options here act like the defaults for all channels.\n" +
    "# Adding any of these options to any channel overrides the options set here.")
  private SharedChannelOptions sharedChannelOptions = new SharedChannelOptions();

  @Setting
  private Map<@NonNull String, @NonNull ChannelOptions> channelOptions =
    Collections.singletonMap("global", ChannelOptions.defaultChannel());

  @Setting
  @Comment("###############################\n" +
    "#      Custom Placeholders    #\n" +
    "###############################\n" +
    "\n" +
    "# Used for message formats - custom tags that are replaced in descending order\n" +
    "# For example, in the default config, <group> wil be replaced with <green>%vault_group% \n" +
    "# <prefix> will be effectively replaced with <gray>[<green>%vault_group%<gray>]")
  private @NonNull Map<@NonNull String, @NonNull String> customPlaceholders =
    ImmutableMap.of("prefix", "<gray>[<group><gray>]", "group", "<green>%vault_group%");

  public SharedChannelOptions defaultChannelOptions() {
    return this.sharedChannelOptions;
  }

  public Map<String, ChannelOptions> channelOptions() {
    return this.channelOptions;
  }

  public Map<String, String> customPlaceholders() {
    return this.customPlaceholders;
  }

  public @Nullable ChannelOptions channelOptions(final @NonNull String name) {
    return this.channelOptions.get(name);
  }

}

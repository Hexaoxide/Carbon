package net.draycia.carbon.api.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Collections;
import java.util.Map;

@ConfigSerializable
public class ChannelSettings {

  private static final ObjectMapper<ChannelSettings> MAPPER;

  static {
    try {
      MAPPER = ObjectMapper.factory().get(ChannelSettings.class);
    } catch (final ObjectMappingException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static ChannelSettings loadFrom(final CommentedConfigurationNode node) throws ObjectMappingException {
    return MAPPER.load(node);
  }

  public void saveTo(final CommentedConfigurationNode node) throws ObjectMappingException {
    MAPPER.save(this, node);
  }

  @Setting
  @Comment("All options here act like the defaults for all channels.\n" +
    "Adding any of these options to any channel overrides the relevant defaults.")
  private SharedChannelOptions sharedChannelOptions = new SharedChannelOptions();

  @Setting
  private Map<String, ChannelOptions> channelOptions =
    Collections.singletonMap("global", ChannelOptions.defaultChannel());

  public SharedChannelOptions defaultChannelOptions() {
    return this.sharedChannelOptions;
  }

  public Map<String, ChannelOptions> channelOptions() {
    return this.channelOptions;
  }

  public ChannelOptions channelOptions(final String name) {
    return this.channelOptions.get(name);
  }

}

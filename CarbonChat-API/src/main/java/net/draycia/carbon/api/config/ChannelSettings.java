package net.draycia.carbon.api.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.util.Collections;
import java.util.Map;

@ConfigSerializable
public class ChannelSettings {

  private static final ObjectMapper<ChannelSettings> MAPPER;

  static {
    try {
      MAPPER = ObjectMapper.forClass(ChannelSettings.class); // We hold on to the instance of our ObjectMapper
    } catch (final ObjectMappingException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static ChannelSettings loadFrom(final CommentedConfigurationNode node) throws ObjectMappingException {
    return MAPPER.bindToNew().populate(node);
  }

  public void saveTo(final CommentedConfigurationNode node) throws ObjectMappingException {
    MAPPER.bind(this).serialize(node);
  }

  @Setting(comment = "All options here act like the defaults for all channels.\nAdding any of these options to any channel overrides the relevant defaults.")
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

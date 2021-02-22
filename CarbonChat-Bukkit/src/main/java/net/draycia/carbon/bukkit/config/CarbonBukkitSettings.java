package net.draycia.carbon.bukkit.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

@ConfigSerializable
public class CarbonBukkitSettings {

  private static final ObjectMapper<CarbonBukkitSettings> MAPPER;

  static {
    try {
      MAPPER = ObjectMapper.factory().get(CarbonBukkitSettings.class);
    } catch (final SerializationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static CarbonBukkitSettings loadFrom(final CommentedConfigurationNode node) throws SerializationException {
    return MAPPER.load(node);
  }

  public void saveTo(final CommentedConfigurationNode node) throws SerializationException {
    MAPPER.save(this, node);
  }

  @Setting
  @Comment("If the plugin will use bungee plugin messaging to show all proxy players in carbon command completions.")
  private boolean bungeePlayerListEnabled;

  public boolean bungeePlayerListEnabled() {
    return this.bungeePlayerListEnabled;
  }

}

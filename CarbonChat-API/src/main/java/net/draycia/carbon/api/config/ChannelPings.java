package net.draycia.carbon.api.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class ChannelPings {

  @Setting
  @Comment("Determines if this feature is enabled")
  private boolean enabled = true;

  @Setting
  @Comment("The text that pings must begin with. A prefix of '@' means you have to type '@Player' to ping Player")
  private @NonNull String prefix = "";

  @Setting
  @Comment("If player names are case sensitive, if true you must type Player to ping Player, 'player' will not work")
  private boolean caseSensitive = false;

  @Setting
  @Comment("The sound played to the pinged player")
  private @NonNull Sound sound = Sound.sound(
    Key.key(Key.MINECRAFT_NAMESPACE, "entity.experience_orb.pickup"),
    Sound.Source.PLAYER,
    10.0F,
    1.0F
  );

  @Setting
  @Comment("The way players will see the pign")
  private @NonNull String display = "<bold><red><ping></red><bold>";

  public boolean enabled() {
    return this.enabled;
  }

  public String prefix() {
    return this.prefix;
  }

  public boolean caseSensitive() {
    return this.caseSensitive;
  }

  public Sound sound() {
    return this.sound;
  }

  public String display() {
    return this.display;
  }

}

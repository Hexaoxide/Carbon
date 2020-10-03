package net.draycia.carbon.api.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

@ConfigSerializable
public final class ChannelPings {

  @Setting(comment = "Determines if this feature is enabled")
  private boolean enabled = true;

  @Setting(comment = "The text that pings must begin with. A prefix of '@' means you have to type '@Player' to ping Player")
  private @NonNull String prefix = "";

  @Setting(comment = "If player names are case sensitive, if true you must type Player to ping Player, 'player' will not work")
  private boolean caseSensitive = false;

  @Setting(comment = "The sound played to the pinged player")
  private @NonNull Sound sound = Sound.sound(
    Key.key(Key.MINECRAFT_NAMESPACE, "entity.experience_orb.pickup"),
    Sound.Source.PLAYER,
    10.0F,
    1.0F
  );

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

}

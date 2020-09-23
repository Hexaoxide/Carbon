package net.draycia.carbon.api.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

@ConfigSerializable
public final class ChannelPings {
  @Setting
  private boolean enabled = true;
  @Setting private @NonNull String prefix = "";
  @Setting private boolean caseSensitive = false;
  @Setting private @NonNull
  Key sound = Key.of(Key.MINECRAFT_NAMESPACE, "entity.experience_orb.pickup");
  @Setting private Sound.Source source = Sound.Source.PLAYER;
  @Setting private float volume = 10.0F;
  @Setting private float pitch = 1.0F;

  public boolean enabled() {
    return this.enabled;
  }

  public String prefix() {
    return this.prefix;
  }

  public boolean caseSensitive() {
    return this.caseSensitive;
  }

  public Key sound() {
    return this.sound;
  }

  public Sound.Source source() {
    return this.source;
  }

  public float volume() {
    return this.volume;
  }

  public float pitch() {
    return this.pitch;
  }
}

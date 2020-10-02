package net.draycia.carbon.api.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

@ConfigSerializable
public final class WhisperPings {

  @Setting private boolean enabled = true;

  @Setting private @NonNull Sound sound = Sound.sound(
    Key.key(Key.MINECRAFT_NAMESPACE, "entity.experience_orb.pickup"),
    Sound.Source.PLAYER,
    10.0F,
    1.0F
  );

  public boolean enabled() {
    return this.enabled;
  }

  public Sound sound() {
    return this.sound;
  }

}

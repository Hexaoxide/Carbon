package net.draycia.carbon.api.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class CarbonSettings {

  @Setting private @NonNull String spyPrefix = "<color>[SPY] ";
  @Setting private @NonNull String serverName = "Server";
  @Setting private boolean showTips = true;
  @Setting private @NonNull List<@NonNull ChannelSettings> channelSettings; // TODO: unfuck this, it's loaded from file?
  @Setting private @Nullable StorageType storageType = StorageType.JSON;
  @Setting private @NonNull SQLCredentials sqlCredentials =
    new SQLCredentials("username", "password", "db", "localhost", 3306);
  @Setting private @NonNull Map<@Nullable String, @NonNull String> customPlaceholders = new HashMap<>();
  @Setting private @NonNull Pings pings = new Pings();
  @Setting private @NonNull WhisperPings whisperPings = new WhisperPings();
  @Setting private @Nullable String channelOnJoin = "";

  public @NonNull String spyPrefix() {
    return this.spyPrefix;
  }

  public @NonNull String serverName() {
    return this.serverName;
  }

  public boolean showTips() {
    return this.showTips;
  }

  public @NonNull List<@NonNull ChannelSettings> channelSettings() {
    return this.channelSettings;
  }

  public @Nullable StorageType storageType() {
    return this.storageType;
  }

  public SQLCredentials sqlCredentials() {
    return this.sqlCredentials;
  }

  public Map<String, String> customPlaceholders() {
    return this.customPlaceholders;
  }

  public @Nullable String channelOnJoin() {
    return this.channelOnJoin;
  }

  public @NonNull Pings pings() {
    return this.pings;
  }

  public @NonNull WhisperPings whisperPings() {
    return this.whisperPings;
  }

  public enum StorageType {
    MYSQL,
    JSON
  }

  @ConfigSerializable
  public final class WhisperPings {
    @Setting private boolean enabled = true;
    @Setting private @NonNull Key sound = Key.of(Key.MINECRAFT_NAMESPACE, "entity.experience_orb.pickup");
    @Setting private Sound.Source source = Sound.Source.PLAYER;
    @Setting private float volume = 10.0F;
    @Setting private float pitch = 1.0F;

    public boolean enabled() {
      return this.enabled;
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

  @ConfigSerializable
  public final class Pings {
    @Setting private boolean enabled = true;
    @Setting private @NonNull String prefix = "";
    @Setting private boolean caseSensitive = false;
    @Setting private @NonNull Key sound = Key.of(Key.MINECRAFT_NAMESPACE, "entity.experience_orb.pickup");
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

}

package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class ExceptionMessages {

  @Setting private @NonNull String channelNotFound = "Channel not found!";
  @Setting private @NonNull String channelNotPublic = "Channel is not public!";
  @Setting private @NonNull String playerNotFound = "Player not found!";

  public String channelNotFound() {
    return this.channelNotFound;
  }

  public String channelNotPublic() {
    return this.channelNotPublic;
  }

  public String playerNotFound() {
    return this.playerNotFound;
  }

}

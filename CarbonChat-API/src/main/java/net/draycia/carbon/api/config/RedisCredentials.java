package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class RedisCredentials {

  @Setting private @NonNull String host = "localhost";
  @Setting private @Nullable String password = "";
  @Setting private int port = 6379;
  @Setting private int database = 0;

  public @NonNull String host() {
    return this.host;
  }

  public @Nullable String password() {
    return this.password;
  }

  public int port() {
    return this.port;
  }

  public int database() {
    return this.database;
  }

}

package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class SQLCredentials {

  @Setting private @NonNull String username = "username";
  @Setting private @NonNull String password = "password";
  @Setting private @NonNull String database = "database";
  @Setting private @NonNull String host = "host";
  @Setting private int port = 3306;

  public @NonNull String username() {
    return this.username;
  }

  public @NonNull String password() {
    return this.password;
  }

  public @NonNull String database() {
    return this.database;
  }

  public @NonNull String host() {
    return this.host;
  }

  public int port() {
    return this.port;
  }

}

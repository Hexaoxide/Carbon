package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RedisCredentials {

  private final @NonNull String host;
  private final @Nullable String password;
  private final int port;
  private final int database;

  public RedisCredentials(final @NonNull String host, final @Nullable String password, final int port, final int database) {
    this.host = host;
    this.password = password;
    this.port = port;
    this.database = database;
  }

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

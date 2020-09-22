package net.draycia.carbon.common.utils;

import org.checkerframework.checker.nullness.qual.NonNull;

public class SQLCredentials {

  private final @NonNull String username;
  private final @NonNull String password;
  private final @NonNull String database;
  private final @NonNull String host;
  private final int port;

  public SQLCredentials(final @NonNull String username, final @NonNull String password,
                        final @NonNull String database, final @NonNull String host, final int port) {
    this.username = username;
    this.password = password;
    this.database = database;
    this.host = host;
    this.port = port;
  }

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

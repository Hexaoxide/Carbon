package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;

public class CarbonSettings {

  private @NonNull String spyPrefix;
  private @NonNull String serverName;
  private boolean showTips;
  private @NonNull List<@NonNull ChannelSettings> channelSettings;
  private @Nullable StorageType storageType;
  private @NonNull SQLCredentials sqlCredentials;
  private @NonNull Map<@Nullable String, @NonNull String> customPlaceholders;

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

  public enum StorageType {
    MYSQL,
    JSON
  }

}

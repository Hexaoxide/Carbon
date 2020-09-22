package net.draycia.carbon.common.channels;

import net.draycia.carbon.api.Context;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ChannelSettings {

  @Nullable
  public String color() {
  }

  @Nullable
  public Map<String, Context> contexts() {
  }

  @Nullable
  public Map<String, String> formats() {
  }

  @Nullable
  public String defaultFormatName() {
  }

  public boolean isDefault() {
  }

  public boolean ignorable() {
  }

  public boolean crossServer() {
  }

  public boolean honorsRecipientList() {
  }

  public boolean permissionGroupMatching() {
  }

  @NonNull
  public List<@NonNull String> groupOverrides() {
  }

  @NonNull
  public String name() {
  }

  @Nullable
  public String messagePrefix() {
  }

  @Nullable
  public String switchMessage() {
  }

  @Nullable
  public String switchOtherMessage() {
  }

  @Nullable
  public String switchFailureMessage() {
  }

  @Nullable
  public String cannotIgnoreMessage() {
  }

  @Nullable
  public String toggleOffMessage() {
  }

  @Nullable
  public String toggleOnMessage() {
  }

  @Nullable
  public String toggleOtherOnMessage() {
  }

  @Nullable
  public String toggleOtherOffMessage() {
  }

  @Nullable
  public String cannotUseMessage() {
  }

  public boolean primaryGroupOnly() {
  }

  public boolean shouldCancelChatEvent() {
  }

  @NonNull
  public List<@NonNull Pattern> itemLinkPatterns() {
  }

  @Nullable
  public String aliases() {
  }

}

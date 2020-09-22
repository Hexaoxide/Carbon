package net.draycia.carbon.api.config;

import net.draycia.carbon.api.Context;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ChannelSettings {

  private String key;
  private String color;
  private Map<String, Context> contexts;
  private Map<String, String> formats;
  private String defaultFormatName;
  private boolean isDefault;
  private boolean ignorable;
  private boolean crossServer;
  private boolean honorsRecipientList;
  private boolean permissionGroupMatching;
  private List<String> groupOverrides;
  private String name;
  private String messagePrefix;
  private String switchMessage;
  private List<Pattern> itemLinkPatterns;
  private String switchOtherMessage;
  private List<String> aliases;
  private boolean shouldCancelChatEvent;
  private boolean primaryGroupOnly;
  private String cannotUseMessage;
  private String toggleOtherOffMessage;
  private String toggleOtherOnMessage;
  private String toggleOnMessage;
  private String toggleOffMessage;
  private String cannotIgnoreMessage;
  private String switchFailureMessage;

  @NonNull
  public String key() {
    return this.key;
  }

  @Nullable
  public String color() {
    return this.color;
  }

  @Nullable
  public Map<String, Context> contexts() {
    return this.contexts;
  }

  @Nullable
  public Map<String, String> formats() {
    return this.formats;
  }

  @Nullable
  public String defaultFormatName() {
    return this.defaultFormatName;
  }

  public boolean isDefault() {
    return this.isDefault;
  }

  public boolean ignorable() {
    return this.ignorable;
  }

  public boolean crossServer() {
    return this.crossServer;
  }

  public boolean honorsRecipientList() {
    return this.honorsRecipientList;
  }

  public boolean permissionGroupMatching() {
    return this.permissionGroupMatching;
  }

  @NonNull
  public List<@NonNull String> groupOverrides() {
    return this.groupOverrides;
  }

  @NonNull
  public String name() {
    if (this.name == null) {
      return this.key;
    }

    return this.name;
  }

  @Nullable
  public String messagePrefix() {
    return this.messagePrefix;
  }

  @Nullable
  public String switchMessage() {
    return this.switchMessage;
  }

  @Nullable
  public String switchOtherMessage() {
    return this.switchOtherMessage;
  }

  @Nullable
  public String switchFailureMessage() {
    return this.switchFailureMessage;
  }

  @Nullable
  public String cannotIgnoreMessage() {
    return this.cannotIgnoreMessage;
  }

  @Nullable
  public String toggleOffMessage() {
    return this.toggleOffMessage;
  }

  @Nullable
  public String toggleOnMessage() {
    return this.toggleOnMessage;
  }

  @Nullable
  public String toggleOtherOnMessage() {
    return this.toggleOtherOnMessage;
  }

  @Nullable
  public String toggleOtherOffMessage() {
    return this.toggleOtherOffMessage;
  }

  @Nullable
  public String cannotUseMessage() {
    return this.cannotUseMessage;
  }

  public boolean primaryGroupOnly() {
    return this.primaryGroupOnly;
  }

  public boolean shouldCancelChatEvent() {
    return this.shouldCancelChatEvent;
  }

  @NonNull
  public List<@NonNull Pattern> itemLinkPatterns() {
    return this.itemLinkPatterns;
  }

  @Nullable
  public List<String> aliases() {
    return this.aliases;
  }

}

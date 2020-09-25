package net.draycia.carbon.api.config;

import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.Context;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class ChannelOptions {

  @Setting private String key = "channel";
  @Setting private String color;
  @Setting private Map<String, Context> contexts = new HashMap<>(); // TODO: set defaults
  @Setting private Map<String, String> formats = new HashMap<>(); // TODO: set defaults
  @Setting private String defaultFormatName;
  @Setting private boolean isDefault = false; // primitive because missing = false
  @Setting private Boolean ignorable; // boxed because missing = use defaults
  @Setting private Boolean crossServer;
  @Setting private Boolean honorsRecipientList;
  @Setting private Boolean permissionGroupMatching;
  @Setting private List<String> groupOverrides;
  @Setting private String name = "";
  @Setting private String messagePrefix = "";
  @Setting private List<String> aliases = new ArrayList<>();
  @Setting private Boolean shouldCancelChatEvent;
  @Setting private Boolean primaryGroupOnly;
  @Setting private String switchMessage;
  @Setting private String switchOtherMessage;
  @Setting private String switchFailureMessage;
  @Setting private String toggleOnMessage;
  @Setting private String toggleOffMessage;
  @Setting private String toggleOtherOnMessage;
  @Setting private String toggleOtherOffMessage;
  @Setting private String cannotUseMessage;
  @Setting private String cannotIgnoreMessage;
  
  private SharedChannelOptions defaultOptions() {
    return CarbonChatProvider.carbonChat().channelSettings().defaultChannelOptions();
  }

  public static ChannelOptions defaultChannel() {
    final ChannelOptions settings = new ChannelOptions();
    settings.key = "global";
    settings.isDefault = true;
    settings.name = "Global";
    settings.formats = Collections.singletonMap("default", "<color><<displayname><reset><color>> <message>");

    return settings;
  }

  @NonNull
  public String key() {
    return this.key;
  }

  @Nullable
  public String color() {
    if (this.color == null) {
      return this.defaultOptions().color();
    }

    return this.color;
  }

  @Nullable
  public Map<String, Context> contexts() {
    // TODO: properly implement default context inheritence
    // TODO: perhaps add an option to make it so overriding any context overrides all?
    if (this.contexts == null) {
      return this.defaultOptions().contexts();
    }

    return this.contexts;
  }

  @Nullable
  public Map<String, String> formats() {
    // TODO: properly implement default format inheritence
    // TODO: perhaps add an option to make it so overriding any format overrides all?
    if (this.formats == null) {
      return this.defaultOptions().formats();
    }

    return this.formats;
  }

  @Nullable
  public String defaultFormatName() {
    if (this.defaultFormatName == null || this.defaultFormatName.isEmpty()) {
      return this.defaultOptions().defaultFormatName();
    }

    return this.defaultFormatName;
  }

  public boolean isDefault() {
    return this.isDefault;
  }

  public Boolean ignorable() {
    if (this.ignorable == null) {
      return this.defaultOptions().ignorable();
    }

    return this.ignorable;
  }

  public Boolean crossServer() {
    if (this.crossServer == null) {
      return this.defaultOptions().crossServer();
    }

    return this.crossServer;
  }

  public Boolean honorsRecipientList() {
    if (this.honorsRecipientList == null) {
      return this.defaultOptions().honorsRecipientList();
    }

    return this.honorsRecipientList;
  }

  public Boolean permissionGroupMatching() {
    if (this.permissionGroupMatching == null) {
      return this.defaultOptions().permissionGroupMatching();
    }

    return this.permissionGroupMatching;
  }

  @NonNull
  public List<@NonNull String> groupOverrides() {
    if (this.groupOverrides == null) {
      return this.defaultOptions().groupOverrides();
    }

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
    if (this.switchMessage == null) {
      return this.defaultOptions().switchMessage();
    }

    return this.switchMessage;
  }

  @Nullable
  public String switchOtherMessage() {
    if (this.switchOtherMessage == null) {
      return this.defaultOptions().switchOtherMessage();
    }

    return this.switchOtherMessage;
  }

  @Nullable
  public String switchFailureMessage() {
    if (this.switchFailureMessage == null) {
      return this.defaultOptions().switchFailureMessage();
    }

    return this.switchFailureMessage;
  }

  @Nullable
  public String cannotIgnoreMessage() {
    if (this.cannotIgnoreMessage == null) {
      return this.defaultOptions().cannotIgnoreMessage();
    }

    return this.cannotIgnoreMessage;
  }

  @Nullable
  public String toggleOffMessage() {
    if (this.toggleOffMessage == null) {
      return this.defaultOptions().toggleOffMessage();
    }

    return this.toggleOffMessage;
  }

  @Nullable
  public String toggleOnMessage() {
    if (this.toggleOnMessage == null) {
      return this.defaultOptions().toggleOnMessage();
    }

    return this.toggleOnMessage;
  }

  @Nullable
  public String toggleOtherOnMessage() {
    if (this.toggleOtherOnMessage == null) {
      return this.defaultOptions().toggleOtherOnMessage();
    }

    return this.toggleOtherOnMessage;
  }

  @Nullable
  public String toggleOtherOffMessage() {
    if (this.toggleOtherOffMessage == null) {
      return this.defaultOptions().toggleOtherOffMessage();
    }

    return this.toggleOtherOffMessage;
  }

  @Nullable
  public String cannotUseMessage() {
    if (this.cannotUseMessage == null) {
      return this.defaultOptions().cannotUseMessage();
    }

    return this.cannotUseMessage;
  }

  public Boolean primaryGroupOnly() {
    if (this.primaryGroupOnly == null) {
      return this.defaultOptions().primaryGroupOnly();
    }

    return this.primaryGroupOnly;
  }

  public Boolean shouldCancelChatEvent() {
    if (this.shouldCancelChatEvent == null) {
      return this.defaultOptions().shouldCancelChatEvent();
    }

    return this.shouldCancelChatEvent;
  }

  @Nullable
  public List<String> aliases() {
    return this.aliases;
  }

}

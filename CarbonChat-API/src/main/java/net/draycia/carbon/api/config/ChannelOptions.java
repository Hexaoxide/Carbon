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

  @Setting(comment = "What this channel is identified as. This will be what's typed ingame to use the channel.")
  private String key = "channel";
  
  @Setting(comment = "This is what the <color> placeholder will typically be replaced with.\n" +
    "Hex RGB (#B19CD9), named colors (light_purple), legacy (&d), and legacy RGB (&x&b&1&2&c&d&9) are all supported.\n" +
    "If on a platform that supports PlaceholderAPI, this option will be ran through that as well.\n" +
    "Note that the <color> placeholder is also used for personal and global user colors.")
  private String color;
  
  @Setting(comment = "The contexts for this channel, ")
  private Map<String, Context> contexts = new HashMap<>(); // TODO: set defaults
  
  @Setting(comment = "The formats for this channel. The key is the name of the group as your permissions plugin reports it.")
  private Map<String, String> formats = new HashMap<>(); // TODO: set defaults
  
  @Setting(comment = "The name of the format that the plugin will fall back to when it cannot find a matching format for the player's groups.")
  private String defaultFormatName;
  
  @Setting(comment = "")
  private boolean isDefault = false; // primitive because missing = false
  
  @Setting(comment = "")
  private Boolean ignorable; // boxed because missing = use defaults
  
  @Setting(comment = "")
  private Boolean crossServer;
  
  @Setting(comment = "")
  private Boolean honorsRecipientList;
  
  @Setting(comment = "")
  private Boolean permissionGroupMatching;
  
  @Setting(comment = "")
  private List<String> groupOverrides;
  
  @Setting(comment = "")
  private String name = "";
  
  @Setting(comment = "")
  private String messagePrefix = "";
  
  @Setting(comment = "")
  private List<String> aliases = new ArrayList<>();
  
  @Setting(comment = "")
  private Boolean shouldCancelChatEvent;
  
  @Setting(comment = "")
  private Boolean primaryGroupOnly;
  
  @Setting(comment = "")
  private String switchMessage;
  
  @Setting(comment = "")
  private String switchOtherMessage;
  
  @Setting(comment = "")
  private String switchFailureMessage;
  
  @Setting(comment = "")
  private String toggleOnMessage;
  
  @Setting(comment = "")
  private String toggleOffMessage;
  
  @Setting(comment = "")
  private String toggleOtherOnMessage;
  
  @Setting(comment = "")
  private String toggleOtherOffMessage;
  
  @Setting(comment = "")
  private String cannotUseMessage;
  
  @Setting(comment = "")
  private String cannotIgnoreMessage;
  
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
  public Context context(final @NonNull String key) {
    final Context localContext;

    if (this.contexts != null) {
      localContext = this.contexts.get(key);
    } else {
      localContext = null;
    }

    if (localContext == null) {
      if (this.defaultOptions().contexts() != null) {
        return this.defaultOptions().contexts().get(key);
      } else {
        return null;
      }
    }

    return localContext;
  }

  @Nullable
  public Map<String, Context> contexts() {
    if (this.contexts == null) {
      return this.defaultOptions().contexts();
    }

    return this.contexts;
  }

  @Nullable
  public Map<String, Context> contextsAndDefault() {
    if (this.contexts == null) {
      return this.defaultOptions().contexts();
    }

    if (this.defaultOptions().contexts() == null) {
      return this.contexts;
    }

    final Map<String, Context> contexts = new HashMap<>(this.defaultOptions().contexts());
    contexts.putAll(this.contexts);

    return contexts;
  }

  @Nullable
  public String format(final @NonNull String key) {
    final String localFormat;

    if (this.formats() != null) {
      localFormat = this.formats().get(key);
    } else {
      localFormat = null;
    }

    if (localFormat == null) {
      if (this.defaultOptions().formats() != null) {
        return this.defaultOptions().formats().get(key);
      } else {
        return null;
      }
    }

    return localFormat;
  }

  @Nullable
  public Map<String, String> formats() {
    if (this.formats == null) {
      return this.defaultOptions().formats();
    }

    return this.formats;
  }

  @Nullable
  public Map<String, String> formatsAndDefault() {
    if (this.formats == null) {
      return this.defaultOptions().formats();
    }

    if (this.defaultOptions().formats() == null) {
      return this.formats;
    }

    final Map<String, String> formats = new HashMap<>(this.defaultOptions().formats());
    formats.putAll(this.formats);

    return formats;
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

package net.draycia.carbon.api.config;

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
  @Setting private String color = "#FFFFFF";
  @Setting private Map<String, Context> contexts = new HashMap<>(); // TODO: set defaults
  @Setting private Map<String, String> formats = new HashMap<>(); // TODO: set defaults
  @Setting private String defaultFormatName = "default";
  @Setting private boolean isDefault = false;
  @Setting private boolean ignorable = true;
  @Setting private boolean crossServer = true;
  @Setting private boolean honorsRecipientList = false;
  @Setting private boolean permissionGroupMatching = false;
  @Setting private List<String> groupOverrides = new ArrayList<>();
  @Setting private String name = "";
  @Setting private String messagePrefix = "";
  @Setting private List<String> aliases = new ArrayList<>();
  @Setting private boolean shouldCancelChatEvent = false;
  @Setting private boolean primaryGroupOnly = false;
  @Setting private String switchMessage = "<gray>You are now in <color><channel> <gray>chat!";
  @Setting private String switchOtherMessage = "<gray><player> <reset><gray>is now in <color><channel> <gray>chat!";
  @Setting private String switchFailureMessage = "<red>You cannot use channel <channel>!";
  @Setting private String toggleOnMessage = "<gray>You can now see <color><channel> <gray>chat!";
  @Setting private String toggleOffMessage = "<gray>You can no longer see <color><channel> <gray>chat!";
  @Setting private String toggleOtherOnMessage = "<gray><player> <reset><gray>can now see <color><channel> <gray>chat!";
  @Setting private String toggleOtherOffMessage = "<gray><player> <reset><gray>can no longer see <color><channel> <gray>chat!";
  @Setting private String cannotUseMessage = "You cannot use that channel!";
  @Setting private String cannotIgnoreMessage = "<red>You cannot ignore that channel!";

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

  @Nullable
  public List<String> aliases() {
    return this.aliases;
  }

}

package net.draycia.carbon.api.config;

import net.draycia.carbon.api.Context;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class SharedChannelOptions {

  @Setting private String color = "#FFFFFF";
  @Setting private Map<String, Context> contexts = new HashMap<>(); // TODO: set defaults
  @Setting private Map<String, String> formats = new HashMap<>(); // TODO: set defaults
  @Setting private String defaultFormatName = "default";
  @Setting private boolean ignorable = true;
  @Setting private boolean crossServer = true;
  @Setting private boolean honorsRecipientList = false;
  @Setting private boolean permissionGroupMatching = false;
  @Setting private List<String> groupOverrides = new ArrayList<>();
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

  public @Nullable String color() {
    return this.color;
  }

  public @Nullable Map<String, Context> contexts() {
    return this.contexts;
  }

  public @Nullable Map<String, String> formats() {
    return this.formats;
  }

  public @Nullable String defaultFormatName() {
    return this.defaultFormatName;
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

  public @NonNull List<@NonNull String> groupOverrides() {
    return this.groupOverrides;
  }

  public @Nullable String switchMessage() {
    return this.switchMessage;
  }

  public @Nullable String switchOtherMessage() {
    return this.switchOtherMessage;
  }

  public @Nullable String switchFailureMessage() {
    return this.switchFailureMessage;
  }

  public @Nullable String cannotIgnoreMessage() {
    return this.cannotIgnoreMessage;
  }

  public @Nullable String toggleOffMessage() {
    return this.toggleOffMessage;
  }

  public @Nullable String toggleOnMessage() {
    return this.toggleOnMessage;
  }

  public @Nullable String toggleOtherOnMessage() {
    return this.toggleOtherOnMessage;
  }

  public @Nullable String toggleOtherOffMessage() {
    return this.toggleOtherOffMessage;
  }

  public @Nullable String cannotUseMessage() {
    return this.cannotUseMessage;
  }

  public boolean primaryGroupOnly() {
    return this.primaryGroupOnly;
  }

  public boolean shouldCancelChatEvent() {
    return this.shouldCancelChatEvent;
  }

}

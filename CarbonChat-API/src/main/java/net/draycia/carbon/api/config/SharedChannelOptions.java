package net.draycia.carbon.api.config;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public final class SharedChannelOptions {

  @Setting private @NonNull String color = "#FFFFFF";
  @Setting private @NonNull Map<@NonNull String, @NonNull String> formats =
    ImmutableMap.of("default", "<color><<displayname><reset><color>> <message>",
      "staff", "<#00CED1>[Staff] <color><<displayname><reset><color>> <message>");
  @Setting private @NonNull String defaultFormatName = "default";
  @Setting private boolean ignorable = true;
  @Setting private boolean crossServer = true;
  @Setting private boolean honorsRecipientList = false;
  @Setting private boolean permissionGroupMatching = false;
  @Setting private @NonNull List<String> groupOverrides = new ArrayList<>();
  @Setting private boolean shouldCancelChatEvent = false;
  @Setting private boolean primaryGroupOnly = false;
  @Setting private @NonNull String switchMessage = "<gray>You are now in <color><channel> <gray>chat!";
  @Setting private @NonNull String switchOtherMessage = "<gray><player> <reset><gray>is now in <color><channel> <gray>chat!";
  @Setting private @NonNull String switchFailureMessage = "<red>You cannot use channel <channel>!";
  @Setting private @NonNull String toggleOnMessage = "<gray>You can now see <color><channel> <gray>chat!";
  @Setting private @NonNull String toggleOffMessage = "<gray>You can no longer see <color><channel> <gray>chat!";
  @Setting private @NonNull String toggleOtherOnMessage = "<gray><player> <reset><gray>can now see <color><channel> <gray>chat!";
  @Setting private @NonNull String toggleOtherOffMessage = "<gray><player> <reset><gray>can no longer see <color><channel> <gray>chat!";
  @Setting private @NonNull String cannotUseMessage = "You cannot use that channel!";
  @Setting private @NonNull String cannotIgnoreMessage = "<red>You cannot ignore that channel!";
  @Setting private @NonNull List<@NonNull ColorPriority> colorPriorities = new ArrayList<>();

  public @NonNull String color() {
    return this.color;
  }

  public @NonNull Map<String, String> formats() {
    return this.formats;
  }

  public @NonNull String defaultFormatName() {
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

  public @NonNull String switchMessage() {
    return this.switchMessage;
  }

  public @NonNull String switchOtherMessage() {
    return this.switchOtherMessage;
  }

  public @NonNull String switchFailureMessage() {
    return this.switchFailureMessage;
  }

  public @NonNull String cannotIgnoreMessage() {
    return this.cannotIgnoreMessage;
  }

  public @NonNull String toggleOffMessage() {
    return this.toggleOffMessage;
  }

  public @NonNull String toggleOnMessage() {
    return this.toggleOnMessage;
  }

  public @NonNull String toggleOtherOnMessage() {
    return this.toggleOtherOnMessage;
  }

  public @NonNull String toggleOtherOffMessage() {
    return this.toggleOtherOffMessage;
  }

  public @NonNull String cannotUseMessage() {
    return this.cannotUseMessage;
  }

  public boolean primaryGroupOnly() {
    return this.primaryGroupOnly;
  }

  public boolean shouldCancelChatEvent() {
    return this.shouldCancelChatEvent;
  }

  public @NonNull List<@NonNull ColorPriority> colorPriorities() {
    return this.colorPriorities;
  }
}

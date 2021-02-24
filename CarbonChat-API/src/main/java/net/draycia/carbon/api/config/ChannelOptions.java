package net.draycia.carbon.api.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.draycia.carbon.api.CarbonChatProvider;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
@SuppressWarnings("initialization.fields.uninitialized")
public final class ChannelOptions {

  @Setting
  @Comment("What this channel is identified as. This is used in command arguments (/channel global)")
  private @NonNull String key = "channel";
  
  @Setting
  @Comment("This is what the <color> placeholder will typically be replaced with.\n" +
    "Hex RGB (#B19CD9), named colors (light_purple), legacy (&d), and legacy RGB (&x&b&1&2&c&d&9) are all supported.\n" +
    "If on a platform that supports PlaceholderAPI, this option will be ran through that as well.\n" +
    "Note that the <color> placeholder is also used for personal and global user colors.")
  private @Nullable String color = NamedTextColor.WHITE.asHexString();
  
  @Setting
  @Comment("The formats for this channel. The key is the name of the group as your permissions plugin reports it.")
  private @Nullable Map<String, String> formats = ImmutableMap.of("default", "<color><<displayname><reset><color>> <message>");

  @Setting
  @Comment("The name of the format that the plugin will fall back to when it cannot find a matching format for the player's groups.")
  private @Nullable String defaultFormatName;
  
  @Setting
  @Comment("If this channel is the default channel players join in.\n" +
    "Also used as a fallback in case the player's selected channel cannot be found.")
  private boolean isDefault = false; // primitive because missing = false
  
  @Setting
  @Comment("If this channel can be ignored / hidden with the /ignore command")
  private Boolean ignorable; // boxed because missing = use defaults
  
  @Setting
  @Comment("If this channel syncs to other servers (cross-server chat), requires a messaging system setup")
  private Boolean crossServer;
  
  @Setting
  @Comment("If players with the permission carbonchat.group.groupname are considered to have the group groupname")
  private Boolean permissionGroupMatching;
  
  @Setting
  @Comment("A custom (ordered) list of group priorities")
  private List<String> groupOverrides;
  
  @Setting
  @Comment("# The display name of this channel, supports minimessage. Used in command feedback (\"You switched to the Global channel\")")
  private String name = "";
  
  @Setting
  @Comment("If the player's chat message starts with whatever this is set to, the player speaks in this channel instead of their selected one")
  private String messagePrefix = "";
  
  @Setting
  @Comment("The command aliases for this channel (/global /g)")
  private List<String> aliases = new ArrayList<>();
  
  @Setting
  @Comment("If the bukkit chat event should be cancelled, you probably don't want to change this")
  private Boolean shouldCancelChatEvent;
  
  @Setting
  @Comment("If the player's format should be decided by their primary group only")
  private Boolean primaryGroupOnly;
  
  @Setting
  @Comment("The message that's sent when switching to this channel")
  private String switchMessage;
  
  @Setting
  @Comment("The message that's sent when forcing other players to switch to this channel")
  private String switchOtherMessage;
  
  @Setting
  @Comment("The message that's sent when you're unable to switch to a channel")
  private String switchFailureMessage;
  
  @Setting
  @Comment("The message that's sent when you are now able to see messages in a channel")
  private String toggleOnMessage;
  
  @Setting
  @Comment("The message that's sent when you can no longer see messages in a channel")
  private String toggleOffMessage;
  
  @Setting
  @Comment("The message that's sent when you toggle another player's channel visibility on")
  private String toggleOtherOnMessage;
  
  @Setting
  @Comment("The message that's sent when you toggle another player's channel visibility off")
  private String toggleOtherOffMessage;
  
  @Setting
  @Comment("The message that's sent when you cannot speak in a channel")
  private String cannotUseMessage;
  
  @Setting
  @Comment("The message that's sent when you attempt to ignore a channel but are unable to do so")
  private String cannotIgnoreMessage;

  @Setting
  @Comment("The order in which colors are chosen for the <color> tag.\n" +
    "Options:\n" +
    "  PLAYER: The color the player has set with /setcolor\n" +
    "  CUSTOM: The color the player has set with /setchannelcolor\n" +
    "  CHANNEL: The color the channel has set in its settings")
  private List<@NonNull ColorPriority> colorPriorities = ImmutableList.of(
    ColorPriority.PLAYER, ColorPriority.CUSTOM, ColorPriority.CHANNEL);
  
  private @NonNull SharedChannelOptions defaultOptions() {
    return CarbonChatProvider.carbonChat().channelSettings().defaultChannelOptions();
  }

  public static ChannelOptions defaultChannel() {
    final ChannelOptions settings = new ChannelOptions();
    settings.key = "global";
    settings.isDefault = true;
    settings.name = "Global";
    settings.color = "#FFFFFF";
    settings.defaultFormatName = "default";
    settings.formats = Collections.singletonMap("default", "<color><<displayname><reset><color>> <message>");
    settings.aliases = Lists.newArrayList("global", "g");

    return settings;
  }

  public @NonNull String key() {
    return this.key;
  }

  public @NonNull String color() {
    if (this.color == null) {
      return this.defaultOptions().color();
    }

    return this.color;
  }

  public @Nullable String format(final @NonNull String key) {
    final String localFormat = this.formats().get(key);

    if (localFormat == null) {
      return this.defaultOptions().formats().get(key);
    }

    return localFormat;
  }

  public @NonNull Map<@NonNull String, @NonNull String> formats() {
    if (this.formats == null) {
      return this.defaultOptions().formats();
    }

    return this.formats;
  }

  public @NonNull Map<@NonNull String, @NonNull String> formatsAndDefault() {
    if (this.formats == null) {
      return this.defaultOptions().formats();
    }

    final Map<String, String> formats = new HashMap<>(this.defaultOptions().formats());

    if (this.formats != null) {
      formats.putAll(this.formats);
    }

    return formats;
  }

  public @NonNull String defaultFormatName() {
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

  public Boolean permissionGroupMatching() {
    if (this.permissionGroupMatching == null) {
      return this.defaultOptions().permissionGroupMatching();
    }

    return this.permissionGroupMatching;
  }

  public @NonNull List<@NonNull String> groupOverrides() {
    if (this.groupOverrides == null || this.groupOverrides.isEmpty()) {
      return this.defaultOptions().groupOverrides();
    }

    return this.groupOverrides;
  }

  public @NonNull String name() {
    if (this.name == null) {
      return this.key;
    }

    return this.name;
  }

  public @Nullable String messagePrefix() {
    return this.messagePrefix;
  }

  public @NonNull String switchMessage() {
    if (this.switchMessage == null) {
      return this.defaultOptions().switchMessage();
    }

    return this.switchMessage;
  }

  public @NonNull String switchOtherMessage() {
    if (this.switchOtherMessage == null) {
      return this.defaultOptions().switchOtherMessage();
    }

    return this.switchOtherMessage;
  }

  public @NonNull String switchFailureMessage() {
    if (this.switchFailureMessage == null) {
      return this.defaultOptions().switchFailureMessage();
    }

    return this.switchFailureMessage;
  }

  public @NonNull String cannotIgnoreMessage() {
    if (this.cannotIgnoreMessage == null) {
      return this.defaultOptions().cannotIgnoreMessage();
    }

    return this.cannotIgnoreMessage;
  }

  public @NonNull String toggleOffMessage() {
    if (this.toggleOffMessage == null) {
      return this.defaultOptions().toggleOffMessage();
    }

    return this.toggleOffMessage;
  }

  public @NonNull String toggleOnMessage() {
    if (this.toggleOnMessage == null) {
      return this.defaultOptions().toggleOnMessage();
    }

    return this.toggleOnMessage;
  }

  public @NonNull String toggleOtherOnMessage() {
    if (this.toggleOtherOnMessage == null) {
      return this.defaultOptions().toggleOtherOnMessage();
    }

    return this.toggleOtherOnMessage;
  }

  public @NonNull String toggleOtherOffMessage() {
    if (this.toggleOtherOffMessage == null) {
      return this.defaultOptions().toggleOtherOffMessage();
    }

    return this.toggleOtherOffMessage;
  }

  public @NonNull String cannotUseMessage() {
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

  public List<ColorPriority> colorPriorities() {
    if (this.colorPriorities == null) {
      return this.defaultOptions().colorPriorities();
    }

    return this.colorPriorities;
  }

  public @NonNull List<String> aliases() {
    return this.aliases;
  }

}

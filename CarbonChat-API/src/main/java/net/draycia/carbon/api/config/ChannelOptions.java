package net.draycia.carbon.api.config;

import com.google.common.collect.ImmutableMap;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.Context;
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
  @Comment("What this channel is identified as. This will be what's typed ingame to use the channel.")
  private @NonNull String key = "channel";
  
  @Setting
  @Comment("This is what the <color> placeholder will typically be replaced with.\n" +
    "Hex RGB (#B19CD9), named colors (light_purple), legacy (&d), and legacy RGB (&x&b&1&2&c&d&9) are all supported.\n" +
    "If on a platform that supports PlaceholderAPI, this option will be ran through that as well.\n" +
    "Note that the <color> placeholder is also used for personal and global user colors.")
  private @Nullable String color = NamedTextColor.WHITE.asHexString();
  
  @Setting
  @Comment("The contexts for this channel, which can modify the behaviour of channels and how/when players can use them.")
  private @Nullable Map<@NonNull String, @NonNull Context> contexts = new HashMap<>();
  
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
  @Comment("If this channel should respect the bukkit recipient list, you normally shouldn't touch this")
  private Boolean honorsRecipientList;
  
  @Setting
  @Comment("If players with the permission carbonchat.group.groupname are considered to have the group groupname")
  private Boolean permissionGroupMatching;
  
  @Setting
  @Comment("A custom (ordered) list of group priorities")
  private List<String> groupOverrides;
  
  @Setting
  @Comment("The display name of this channel, supports minimessage. Used in command feedback")
  private String name = "";
  
  @Setting
  @Comment("If the player's chat message starts with whatever this is set to, the player speaks in this channel instead of their selected one")
  private String messagePrefix = "";
  
  @Setting
  @Comment("The command aliases for this channel")
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
  
  private @NonNull SharedChannelOptions defaultOptions() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    if (carbonChat == null) {
      throw new IllegalStateException("CarbonChat not initialized!");
    }

    return carbonChat.channelSettings().defaultChannelOptions();
  }

  public static ChannelOptions defaultChannel() {
    final ChannelOptions settings = new ChannelOptions();
    settings.key = "global";
    settings.isDefault = true;
    settings.name = "Global";
    settings.color = "#FFFFFF";
    settings.defaultFormatName = "default";
    settings.formats = Collections.singletonMap("default", "<color><<displayname><reset><color>> <message>");

    return settings;
  }

  public @NonNull String key() {
    return this.key;
  }

  public @Nullable String color() {
    if (this.color == null) {
      return this.defaultOptions().color();
    }

    return this.color;
  }

  public @Nullable Context context(final @NonNull String key) {
    final Context localContext;

    if (this.contexts != null) {
      localContext = this.contexts.get(key);
    } else {
      localContext = null;
    }

    if (localContext == null) {
      final Map<String, Context> defaultContexts = this.defaultOptions().contexts();

      if (defaultContexts != null) {
        return defaultContexts.get(key);
      } else {
        return null;
      }
    }

    return localContext;
  }

  public @Nullable Map<String, Context> contexts() {
    if (this.contexts == null) {
      return this.defaultOptions().contexts();
    }

    return this.contexts;
  }

  public @Nullable Map<String, Context> contextsAndDefault() {
    if (this.contexts == null) {
      return this.defaultOptions().contexts();
    }

    final Map<String, Context> defaultContexts = this.defaultOptions().contexts();

    if (defaultContexts == null) {
      return this.contexts;
    }

    final Map<String, Context> contexts = new HashMap<>(defaultContexts);

    if (this.contexts != null) {
      contexts.putAll(this.contexts);
    }

    return contexts;
  }

  public @Nullable String format(final @NonNull String key) {
    final String localFormat;

    final Map<String, String> localFormats = this.formats();

    if (localFormats != null) {
      localFormat = localFormats.get(key);
    } else {
      localFormat = null;
    }

    if (localFormat == null) {
      final Map<String, String> defaultFormats = this.defaultOptions().formats();

      if (defaultFormats != null) {
        return defaultFormats.get(key);
      } else {
        return null;
      }
    }

    return localFormat;
  }

  public @Nullable Map<@NonNull String, @NonNull String> formats() {
    if (this.formats == null) {
      return this.defaultOptions().formats();
    }

    return this.formats;
  }

  public @Nullable Map<@NonNull String, @NonNull String> formatsAndDefault() {
    if (this.formats == null) {
      return this.defaultOptions().formats();
    }

    final Map<String, String> defaultFormats = this.defaultOptions().formats();

    if (defaultFormats == null) {
      return this.formats;
    }

    final Map<String, String> formats = new HashMap<>(defaultFormats);

    if (this.formats != null) {
      formats.putAll(this.formats);
    }

    return formats;
  }

  public @Nullable String defaultFormatName() {
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

  public @NonNull List<@NonNull String> groupOverrides() {
    if (this.groupOverrides == null) {
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

  public @Nullable String switchMessage() {
    if (this.switchMessage == null) {
      return this.defaultOptions().switchMessage();
    }

    return this.switchMessage;
  }

  public @Nullable String switchOtherMessage() {
    if (this.switchOtherMessage == null) {
      return this.defaultOptions().switchOtherMessage();
    }

    return this.switchOtherMessage;
  }

  public @Nullable String switchFailureMessage() {
    if (this.switchFailureMessage == null) {
      return this.defaultOptions().switchFailureMessage();
    }

    return this.switchFailureMessage;
  }

  public @Nullable String cannotIgnoreMessage() {
    if (this.cannotIgnoreMessage == null) {
      return this.defaultOptions().cannotIgnoreMessage();
    }

    return this.cannotIgnoreMessage;
  }

  public @Nullable String toggleOffMessage() {
    if (this.toggleOffMessage == null) {
      return this.defaultOptions().toggleOffMessage();
    }

    return this.toggleOffMessage;
  }

  public @Nullable String toggleOnMessage() {
    if (this.toggleOnMessage == null) {
      return this.defaultOptions().toggleOnMessage();
    }

    return this.toggleOnMessage;
  }

  public @Nullable String toggleOtherOnMessage() {
    if (this.toggleOtherOnMessage == null) {
      return this.defaultOptions().toggleOtherOnMessage();
    }

    return this.toggleOtherOnMessage;
  }

  public @Nullable String toggleOtherOffMessage() {
    if (this.toggleOtherOffMessage == null) {
      return this.defaultOptions().toggleOtherOffMessage();
    }

    return this.toggleOtherOffMessage;
  }

  public @Nullable String cannotUseMessage() {
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

  public @Nullable List<String> aliases() {
    return this.aliases;
  }

}

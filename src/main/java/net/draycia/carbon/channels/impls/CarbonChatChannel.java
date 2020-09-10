package net.draycia.carbon.channels.impls;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.events.ChatFormatEvent;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CarbonChatChannel extends ChatChannel {

  @NonNull
  private static final Pattern MESSAGE_PATTERN = Pattern.compile("<message>");

  @NonNull
  private final String key;

  @NonNull
  private final CarbonChat carbonChat;

  @Nullable
  private final ConfigurationSection config;

  public CarbonChatChannel(@NonNull final String key, @NonNull final CarbonChat carbonChat, @Nullable final ConfigurationSection config) {
    this.key = key;
    this.carbonChat = carbonChat;
    this.config = config;
  }

  @NonNull
  public CarbonChat getCarbonChat() {
    return carbonChat;
  }

  @Override
  public boolean testContext(@NonNull final ChatUser sender, @NonNull final ChatUser target) {
    return this.carbonChat.getContextManager().testContext(sender, target, this);
  }

  @Override
  public boolean canPlayerUse(@NonNull final ChatUser user) {
    return user.player().hasPermission("carbonchat.channels." + name() + ".use");
  }

  @Override
  @NonNull
  public List<@NonNull ChatUser> audiences() {
    List<ChatUser> audience = new ArrayList<>();

    for (Player player : Bukkit.getOnlinePlayers()) {
      ChatUser playerUser = this.carbonChat.getUserService().wrap(player);

      if (canPlayerSee(playerUser, true)) {
        audience.add(playerUser);
      }
    }

    return audience;
  }

  private void updateUserNickname(@NonNull ChatUser user) {
    if (user.online()) {
      String nickname = user.nickname();

      if (nickname != null) {
        Component component = this.carbonChat.getAdventureManager().processMessage(nickname);
        nickname = CarbonChat.LEGACY.serialize(component);

        user.player().setDisplayName(nickname);

        if (carbonChat.getConfig().getBoolean("nicknames-set-tab-name")) {
          user.player().setPlayerListName(nickname);
        }
      }
    }
  }

  @Override
  @NonNull
  public Component sendMessage(@NonNull ChatUser user, @NonNull Collection<@NonNull ChatUser> recipients, @NonNull String message, boolean fromRemote) {
    updateUserNickname(user);

    // Get player's formatting
    String messageFormat = getFormat(user);

    // Call custom chat event
    PreChatFormatEvent preFormatEvent = new PreChatFormatEvent(user, this, messageFormat, message);
    Bukkit.getPluginManager().callEvent(preFormatEvent);

    // Return if cancelled or message is emptied

    if (preFormatEvent.isCancelled() || preFormatEvent.message().trim().isEmpty()) {
      return TextComponent.empty();
    }

    String displayName;

    if (user.nickname() != null) {
      displayName = user.nickname();
    } else {
      if (user.online()) {
        displayName = user.player().getDisplayName();
      } else {
        displayName = user.offlinePlayer().getName();
      }
    }

    // Iterate through players who should receive messages in this channel
    for (ChatUser target : recipients) {
      // Call second format event. Used for relational stuff (placeholders etc)
      ChatFormatEvent formatEvent = new ChatFormatEvent(user, target, this, preFormatEvent.format(), preFormatEvent.message());
      Bukkit.getPluginManager().callEvent(formatEvent);

      // Again, return if cancelled or message is emptied
      if (formatEvent.isCancelled() || formatEvent.message().trim().isEmpty()) {
        continue;
      }

      TextColor targetColor = channelColor(target);

      TextComponent formatComponent = (TextComponent) this.carbonChat.getAdventureManager().processMessage(formatEvent.format(),
        "br", "\n",
        "displayname", displayName,
        "color", "<" + targetColor.asHexString() + ">",
        "phase", Long.toString(System.currentTimeMillis() % 25),
        "server", this.carbonChat.getConfig().getString("server-name", "Server"),
        "message", formatEvent.message());

      if (isUserSpying(user, target)) {
        String prefix = processPlaceholders(user, this.carbonChat.getConfig().getString("spy-prefix"));

        formatComponent = (TextComponent) MiniMessage.get().parse(prefix, "color",
          targetColor.asHexString()).append(formatComponent);
      }

      ChatComponentEvent newEvent = new ChatComponentEvent(user, target, this, formatComponent,
        formatEvent.message());

      Bukkit.getPluginManager().callEvent(newEvent);

      target.sendMessage(newEvent.component());
    }

    ChatFormatEvent consoleFormatEvent = new ChatFormatEvent(user, null, this, preFormatEvent.format(),
      preFormatEvent.message());

    Bukkit.getPluginManager().callEvent(consoleFormatEvent);

    TextColor targetColor = channelColor(user);

    TextComponent consoleFormat = (TextComponent) this.carbonChat.getAdventureManager().processMessage(consoleFormatEvent.format(),
      "br", "\n",
      "displayname", displayName,
      "color", "<" + targetColor.asHexString() + ">",
      "phase", Long.toString(System.currentTimeMillis() % 25),
      "server", this.carbonChat.getConfig().getString("server-name", "Server"),
      "message", consoleFormatEvent.message());

    ChatComponentEvent consoleEvent = new ChatComponentEvent(user, null, this, consoleFormat,
      consoleFormatEvent.message());

    Bukkit.getPluginManager().callEvent(consoleEvent);

    // Route message to bungee / discord (if message originates from this server)
    // Use instanceof and not isOnline, if this message originates from another then the instanceof will
    // fail, but isOnline may succeed if the player is online on both servers (somehow).
    if (user.online() && !fromRemote && (bungee() || crossServer())) {
      sendMessageToBungee(user.player(), consoleEvent.component());
    }

    return consoleEvent.component();
  }

  @Override
  @NonNull
  public Component sendMessage(@NonNull ChatUser user, @NonNull String message, boolean fromRemote) {
    return this.sendMessage(user, this.audiences(), message, fromRemote);
  }

  @Nullable
  public String getFormat(@NonNull ChatUser user) {
    for (String group : this.groupOverrides()) {
      if (userHasGroup(user, group)) {
        String format = format(group);

        if (format != null) {
          return format;
        }
      }
    }

    if (primaryGroupOnly()) {
      return getPrimaryGroupFormat(user);
    } else {
      return getFirstFoundUserFormat(user);
    }
  }

  private boolean userHasGroup(@NonNull ChatUser user, @NonNull String group) {
    if (user.online()) {
      if (carbonChat.getPermission().playerInGroup(user.player(), group)) {
        return true;
      }
    } else {
      if (carbonChat.getPermission().playerInGroup(null, user.offlinePlayer(), group)) {
        return true;
      }
    }

    if (user.online() && this.permissionGroupMatching()) {
      return user.player().hasPermission("carbonchat.group." + group);
    }

    return false;
  }

  @Nullable
  private String getFirstFoundUserFormat(@NonNull ChatUser user) {
    String[] playerGroups;

    if (user.online()) {
      playerGroups = this.carbonChat.getPermission().getPlayerGroups(user.player());
    } else {
      playerGroups = this.carbonChat.getPermission().getPlayerGroups(null, user.offlinePlayer());
    }

    for (String group : playerGroups) {
      String groupFormat = format(group);

      if (groupFormat != null) {
        return groupFormat;
      }
    }

    return getDefaultFormat();
  }

  @Nullable
  private String getPrimaryGroupFormat(@NonNull ChatUser user) {
    String primaryGroup;

    if (user.online()) {
      primaryGroup = this.carbonChat.getPermission().getPrimaryGroup(user.player());
    } else {
      primaryGroup = this.carbonChat.getPermission().getPrimaryGroup(null, user.offlinePlayer());
    }

    String primaryGroupFormat = format(primaryGroup);

    if (primaryGroupFormat != null) {
      return primaryGroupFormat;
    }

    return getDefaultFormat();
  }

  @Nullable
  private String getDefaultFormat() {
    return format(getDefaultFormatName());
  }

  @Override
  @Nullable
  public String format(@NonNull String group) {
    return getString("formats." + group);
  }

  private boolean isUserSpying(@NonNull ChatUser sender, @NonNull ChatUser target) {
    if (!canPlayerSee(sender, target, false)) {
      return target.channelSettings(this).spying();
    }

    return false;
  }

  @Override
  public void sendComponent(@NonNull ChatUser player, @NonNull Component component) {
    for (ChatUser user : audiences()) {
      if (!user.ignoringUser(player)) {
        user.sendMessage(component);
      }
    }
  }

  public void sendMessageToBungee(@NonNull Player player, @NonNull Component component) {
    this.carbonChat.getMessageManager().sendMessage("channel-component", player.getUniqueId(), (byteArray) -> {
      byteArray.writeUTF(this.key());
      byteArray.writeUTF(carbonChat.getAdventureManager().audiences().gsonSerializer().serialize(component));
    });
  }

  @Override
  public boolean canPlayerSee(@NonNull ChatUser target, boolean checkSpying) {
    Player targetPlayer = target.player();

    if (checkSpying && targetPlayer.hasPermission("carbonchat.spy." + name())) {
      if (target.channelSettings(this).spying()) {
        return true;
      }
    }

    if (!targetPlayer.hasPermission("carbonchat.channels." + name() + ".see")) {
      return false;
    }

    if (ignorable()) {
      return !target.channelSettings(this).ignored();
    }

    return true;
  }

  public boolean canPlayerSee(@NonNull ChatUser sender, @NonNull ChatUser target, boolean checkSpying) {
    Player targetPlayer = target.player();

    if (!canPlayerSee(target, checkSpying)) {
      return false;
    }

    if (ignorable()) {
      return !target.ignoringUser(sender) || targetPlayer.hasPermission("carbonchat.ignore.exempt");
    }

    return true;
  }

  @Override
  @Nullable
  public TextColor channelColor(@NonNull ChatUser user) {
    TextColor userColor = user.channelSettings(this).color();

    if (userColor != null) {
      System.out.println("user color found!");
      return userColor;
    }

    String input = getString("color");

    TextColor color = CarbonUtils.parseColor(user, input);

    if (color == null && this.carbonChat.getConfig().getBoolean("show-tips")) {
      this.carbonChat.getLogger().warning("Tip: Channel color found (" + color + ") is invalid!");
      this.carbonChat.getLogger().warning("Falling back to #FFFFFF");

      return NamedTextColor.WHITE;
    }

    return color;
  }

  @Nullable
  private String getDefaultFormatName() {
    return getString("default-group");
  }

  @Override
  public boolean isDefault() {
    if (config != null && config.contains("default")) {
      return config.getBoolean("default");
    }

    return false;
  }

  @Override
  public boolean ignorable() {
    return getBoolean("ignorable");
  }

  @Override
  @Deprecated
  public boolean bungee() {
    return getBoolean("should-bungee");
  }

  @Override
  public boolean crossServer() {
    return getBoolean("is-cross-server");
  }

  @Override
  public boolean honorsRecipientList() {
    return getBoolean("honors-recipient-list");
  }

  @Override
  public boolean permissionGroupMatching() {
    return getBoolean("permission-group-matching");
  }

  @Override
  @NonNull
  public List<@NonNull String> groupOverrides() {
    return getStringList("group-overrides");
  }

  @Override
  @NonNull
  public String name() {
    String name = getString("name");
    return name == null ? key : name;
  }

  @Override
  @Nullable
  public String messagePrefix() {
    if (config != null && config.contains("message-prefix")) {
      return config.getString("message-prefix");
    }

    return null;
  }

  @Override
  @Nullable
  public String switchMessage() {
    return getString("switch-message");
  }

  @Override
  @Nullable
  public String switchOtherMessage() {
    return getString("switch-other-message");
  }

  @Override
  @Nullable
  public String switchFailureMessage() {
    return getString("switch-failure-message");
  }

  @Override
  @Nullable
  public String cannotIgnoreMessage() {
    return getString("cannot-ignore-message");
  }

  @Override
  @Nullable
  public String toggleOffMessage() {
    return getString("toggle-off-message");
  }

  @Override
  @Nullable
  public String toggleOnMessage() {
    return getString("toggle-on-message");
  }

  @Override
  @Nullable
  public String toggleOtherOnMessage() {
    return getString("toggle-other-on");
  }

  @Override
  @Nullable
  public String toggleOtherOffMessage() {
    return getString("toggle-other-off");
  }

  @Override
  @Nullable
  public String cannotUseMessage() {
    return getString("cannot-use-channel");
  }

  @Override
  public boolean primaryGroupOnly() {
    return getBoolean("primary-group-only");
  }

  @Override
  @NonNull
  public List<@NonNull Pattern> itemLinkPatterns() {
    ArrayList<Pattern> itemPatterns = new ArrayList<>();

    for (String entry : this.carbonChat.getConfig().getStringList("item-link-placeholders")) {
      itemPatterns.add(Pattern.compile(Pattern.quote(entry)));
    }

    return itemPatterns;
  }

  @Override
  @Nullable
  public Object context(@NonNull String key) {
    ConfigurationSection section = config == null ? null : config.getConfigurationSection("contexts");

    if (section == null) {
      ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

      if (defaultSection == null) {
        return null;
      }

      ConfigurationSection defaultContexts = defaultSection.getConfigurationSection("contexts");

      if (defaultContexts == null) {
        return null;
      }

      return defaultContexts.get(key);
    }

    return section.get(key);
  }

  @Nullable
  private String getString(@NonNull String key) {
    if (config != null && config.contains(key)) {
      return config.getString(key);
    }

    ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

    if (defaultSection != null && defaultSection.contains(key)) {
      return defaultSection.getString(key);
    }

    return null;
  }

  @NonNull
  private List<@NonNull String> getStringList(@NonNull String key) {
    if (config != null && config.contains(key)) {
      return config.getStringList(key);
    }

    ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

    if (defaultSection != null && defaultSection.contains(key)) {
      return defaultSection.getStringList(key);
    }

    return Collections.emptyList();
  }

  private boolean getBoolean(@NonNull String key) {
    if (config != null && config.contains(key)) {
      return config.getBoolean(key);
    }

    ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

    if (defaultSection != null && defaultSection.contains(key)) {
      return defaultSection.getBoolean(key);
    }

    return false;
  }

  private double getDouble(@NonNull String key) {
    if (config != null && config.contains(key)) {
      return config.getDouble(key);
    }

    ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

    if (defaultSection != null && defaultSection.contains(key)) {
      return defaultSection.getDouble(key);
    }

    return 0;
  }

  @Override
  @NonNull
  public String key() {
    return key;
  }

  @Override
  @Nullable
  public String aliases() {
    String aliases = getString("aliases");

    if (aliases == null) {
      return key();
    }

    return aliases;
  }

  @Override
  public boolean shouldCancelChatEvent() {
    return getBoolean("cancel-message-event");
  }
}

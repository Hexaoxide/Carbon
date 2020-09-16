package net.draycia.carbon.channels;

import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatComponentEvent;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.draycia.carbon.api.events.MessageContextEvent;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.draycia.carbon.api.events.ReceiverContextEvent;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CarbonChatChannel implements ChatChannel {

  @NonNull
  private final String key;

  @NonNull
  private final CarbonChatBukkit carbonChat;

  @Nullable
  private final ConfigurationSection config;

  public CarbonChatChannel(@NonNull final String key, @NonNull final CarbonChatBukkit carbonChat, @Nullable final ConfigurationSection config) {
    this.key = key;
    this.carbonChat = carbonChat;
    this.config = config;
  }

  @NonNull
  public CarbonChatBukkit carbonChat() {
    return this.carbonChat;
  }

  @Override
  public boolean testContext(@NonNull final ChatUser sender, @NonNull final ChatUser target) {
    final ReceiverContextEvent event = new ReceiverContextEvent(this, sender, target);

    CarbonEvents.post(event);

    return !event.cancelled();
  }

  @Override
  public boolean canPlayerUse(@NonNull final ChatUser user) {
    return user.permissible() && user.hasPermission("carbonchat.channels." + this.name() + ".use");
  }

  @Override
  @NonNull
  public List<@NonNull ChatUser> audiences() {
    final List<ChatUser> audience = new ArrayList<>();

    for (final Player player : Bukkit.getOnlinePlayers()) {
      final ChatUser playerUser = this.carbonChat.userService().wrap(player.getUniqueId());

      if (this.canPlayerSee(playerUser, true)) {
        audience.add(playerUser);
      }
    }

    return audience;
  }

  private void updateUserNickname(@NonNull final ChatUser user) {
    final Player player = Bukkit.getPlayer(user.uuid());

    if (player != null) {
      String nickname = user.nickname();

      if (nickname != null) {
        final Component component = this.carbonChat.messageProcessor().processMessage(nickname);
        nickname = CarbonChatBukkit.LEGACY.serialize(component);

        player.setDisplayName(nickname);

        if (this.carbonChat.getConfig().getBoolean("nicknames-set-tab-name")) {
          player.setPlayerListName(nickname);
        }
      }
    }
  }

  @Override
  @NonNull
  public Component sendMessage(@NonNull final ChatUser user, @NonNull final Collection<@NonNull ChatUser> recipients, @NonNull final String message, final boolean fromRemote) {
    this.updateUserNickname(user);

    final MessageContextEvent event = new MessageContextEvent(this, user);

    CarbonEvents.post(event);

    if (event.cancelled()) {
      return TextComponent.empty();
    }

    // Get player's formatting
    final String messageFormat = this.format(user);

    // Call custom chat event
    final PreChatFormatEvent preFormatEvent = new PreChatFormatEvent(user, this, messageFormat, message);

    CarbonEvents.post(preFormatEvent);

    // Return if cancelled or message is emptied

    if (preFormatEvent.cancelled() || preFormatEvent.message().trim().isEmpty()) {
      return TextComponent.empty();
    }

    final String displayName;

    if (user.nickname() != null) {
      displayName = user.nickname();
    } else {
      final OfflinePlayer player = Bukkit.getOfflinePlayer(user.uuid());

      if (player.isOnline()) {
        displayName = player.getPlayer().getDisplayName();
      } else {
        displayName = player.getName();
      }
    }

    // Iterate through players who should receive messages in this channel
    for (final ChatUser target : recipients) {
      // Call second format event. Used for relational stuff (placeholders etc)
      final ChatFormatEvent formatEvent = new ChatFormatEvent(user, target, this, preFormatEvent.format(), preFormatEvent.message());

      CarbonEvents.post(formatEvent);

      // Again, return if cancelled or message is emptied
      if (formatEvent.cancelled() || formatEvent.message().trim().isEmpty()) {
        continue;
      }

      final TextColor targetColor = this.channelColor(target);

      TextComponent formatComponent = (TextComponent) this.carbonChat.messageProcessor().processMessage(formatEvent.format(),
        "br", "\n",
        "displayname", displayName,
        "color", "<" + targetColor.asHexString() + ">",
        "phase", Long.toString(System.currentTimeMillis() % 25),
        "server", this.carbonChat.getConfig().getString("server-name", "Server"),
        "message", formatEvent.message());

      if (this.isUserSpying(user, target)) {
        final String prefix = this.carbonChat.getConfig().getString("spy-prefix");

        formatComponent = (TextComponent) MiniMessage.get().parse(prefix, "color",
          targetColor.asHexString()).append(formatComponent);
      }

      final ChatComponentEvent newEvent = new ChatComponentEvent(user, target, this, formatComponent,
        formatEvent.message());

      CarbonEvents.post(newEvent);

      target.sendMessage(newEvent.component());
    }

    final ChatFormatEvent consoleFormatEvent = new ChatFormatEvent(user, null, this, preFormatEvent.format(),
      preFormatEvent.message());

    CarbonEvents.post(consoleFormatEvent);

    final TextColor targetColor = this.channelColor(user);

    final TextComponent consoleFormat = (TextComponent) this.carbonChat.messageProcessor().processMessage(consoleFormatEvent.format(),
      "br", "\n",
      "displayname", displayName,
      "color", "<" + targetColor.asHexString() + ">",
      "phase", Long.toString(System.currentTimeMillis() % 25),
      "server", this.carbonChat.getConfig().getString("server-name", "Server"),
      "message", consoleFormatEvent.message());

    final ChatComponentEvent consoleEvent = new ChatComponentEvent(user, null, this, consoleFormat,
      consoleFormatEvent.message());

    CarbonEvents.post(consoleEvent);

    // Route message to bungee / discord (if message originates from this server)
    // Use instanceof and not isOnline, if this message originates from another then the instanceof will
    // fail, but isOnline may succeed if the player is online on both servers (somehow).
    final Player player = Bukkit.getPlayer(user.uuid());

    if (player != null && !fromRemote && this.crossServer()) {
      this.sendMessageToBungee(player, consoleEvent.component());
    }

    return consoleEvent.component();
  }

  @Override
  @NonNull
  public Component sendMessage(@NonNull final ChatUser user, @NonNull final String message, final boolean fromRemote) {
    return this.sendMessage(user, this.audiences(), message, fromRemote);
  }

  @Nullable
  public String format(@NonNull final ChatUser user) {
    for (final String group : this.groupOverrides()) {
      if (this.userHasGroup(user, group)) {
        final String format = this.format(group);

        if (format != null) {
          return format;
        }
      }
    }

    if (this.primaryGroupOnly()) {
      return this.primaryGroupFormat(user);
    } else {
      return this.firstFoundUserFormat(user);
    }
  }

  private boolean userHasGroup(@NonNull final ChatUser user, @NonNull final String group) {
    if (user.permissible()) {
      if (this.carbonChat.permission().playerInGroup(Bukkit.getPlayer(user.uuid()), group)) {
        return true;
      }
    } else {
      if (this.carbonChat.permission().playerInGroup(null, Bukkit.getOfflinePlayer(user.uuid()), group)) {
        return true;
      }
    }

    if (user.permissible() && this.permissionGroupMatching()) {
      return user.hasPermission("carbonchat.group." + group);
    }

    return false;
  }

  @Nullable
  private String firstFoundUserFormat(@NonNull final ChatUser user) {
    final String[] playerGroups;

    if (user.permissible()) {
      playerGroups = this.carbonChat.permission().getPlayerGroups(Bukkit.getPlayer(user.uuid()));
    } else {
      playerGroups = this.carbonChat.permission().getPlayerGroups(null, Bukkit.getOfflinePlayer(user.uuid()));
    }

    for (final String group : playerGroups) {
      final String groupFormat = this.format(group);

      if (groupFormat != null) {
        return groupFormat;
      }
    }

    return this.defaultFormat();
  }

  @Nullable
  private String primaryGroupFormat(@NonNull final ChatUser user) {
    final String primaryGroup;

    if (user.permissible()) {
      primaryGroup = this.carbonChat.permission().getPrimaryGroup(Bukkit.getPlayer(user.uuid()));
    } else {
      primaryGroup = this.carbonChat.permission().getPrimaryGroup(null, Bukkit.getOfflinePlayer(user.uuid()));
    }

    final String primaryGroupFormat = this.format(primaryGroup);

    if (primaryGroupFormat != null) {
      return primaryGroupFormat;
    }

    return this.defaultFormat();
  }

  @Nullable
  private String defaultFormat() {
    return this.format(this.defaultFormatName());
  }

  @Override
  @Nullable
  public String format(@NonNull final String group) {
    return this.getString("formats." + group);
  }

  private boolean isUserSpying(@NonNull final ChatUser sender, @NonNull final ChatUser target) {
    if (!this.canPlayerSee(sender, target, false)) {
      return target.channelSettings(this).spying();
    }

    return false;
  }

  @Override
  public void sendComponent(@NonNull final ChatUser player, @NonNull final Component component) {
    for (final ChatUser user : this.audiences()) {
      if (!user.ignoringUser(player)) {
        user.sendMessage(component);
      }
    }
  }

  public void sendMessageToBungee(@NonNull final Player player, @NonNull final Component component) {
    this.carbonChat.messageManager().sendMessage("channel-component", player.getUniqueId(), byteArray -> {
      byteArray.writeUTF(this.key());
      byteArray.writeUTF(this.carbonChat.messageProcessor().audiences().gsonSerializer().serialize(component));
    });
  }

  @Override
  public boolean canPlayerSee(@NonNull final ChatUser target, final boolean checkSpying) {
    if (checkSpying && target.permissible() && target.hasPermission("carbonchat.spy." + this.name())) {
      if (target.channelSettings(this).spying()) {
        return true;
      }
    }

    if (!(target.permissible() && target.hasPermission("carbonchat.channels." + this.name() + ".see"))) {
      return false;
    }

    if (this.ignorable()) {
      return !target.channelSettings(this).ignored();
    }

    return true;
  }

  public boolean canPlayerSee(@NonNull final ChatUser sender, @NonNull final ChatUser target, final boolean checkSpying) {

    if (!this.canPlayerSee(target, checkSpying)) {
      return false;
    }

    if (this.ignorable()) {
      return !target.ignoringUser(sender) || (target.permissible() && target.hasPermission("carbonchat.ignore.exempt"));
    }

    return true;
  }

  @Override
  @Nullable
  public TextColor channelColor(@NonNull final ChatUser user) {
    final TextColor userColor = user.channelSettings(this).color();

    if (userColor != null) {
      return userColor;
    }

    final String input = this.getString("color");

    final TextColor color = CarbonUtils.parseColor(user, input);

    if (color == null && this.carbonChat.getConfig().getBoolean("show-tips")) {
      this.carbonChat.getLogger().warning("Tip: Channel color found (" + input + ") is invalid!");
      this.carbonChat.getLogger().warning("Falling back to #FFFFFF");

      return NamedTextColor.WHITE;
    }

    return color;
  }

  @Nullable
  private String defaultFormatName() {
    return this.getString("default-group");
  }

  @Override
  public boolean isDefault() {
    if (this.config != null && this.config.contains("default")) {
      return this.config.getBoolean("default");
    }

    return false;
  }

  @Override
  public boolean ignorable() {
    return this.getBoolean("ignorable");
  }

  @Override
  public boolean crossServer() {
    return this.getBoolean("is-cross-server");
  }

  @Override
  public boolean honorsRecipientList() {
    return this.getBoolean("honors-recipient-list");
  }

  @Override
  public boolean permissionGroupMatching() {
    return this.getBoolean("permission-group-matching");
  }

  @Override
  @NonNull
  public List<@NonNull String> groupOverrides() {
    return this.getStringList("group-overrides");
  }

  @Override
  @NonNull
  public String name() {
    final String name = this.getString("name");
    return name == null ? this.key : name;
  }

  @Override
  @Nullable
  public String messagePrefix() {
    if (this.config != null && this.config.contains("message-prefix")) {
      return this.config.getString("message-prefix");
    }

    return null;
  }

  @Override
  @Nullable
  public String switchMessage() {
    return this.getString("switch-message");
  }

  @Override
  @Nullable
  public String switchOtherMessage() {
    return this.getString("switch-other-message");
  }

  @Override
  @Nullable
  public String switchFailureMessage() {
    return this.getString("switch-failure-message");
  }

  @Override
  @Nullable
  public String cannotIgnoreMessage() {
    return this.getString("cannot-ignore-message");
  }

  @Override
  @Nullable
  public String toggleOffMessage() {
    return this.getString("toggle-off-message");
  }

  @Override
  @Nullable
  public String toggleOnMessage() {
    return this.getString("toggle-on-message");
  }

  @Override
  @Nullable
  public String toggleOtherOnMessage() {
    return this.getString("toggle-other-on");
  }

  @Override
  @Nullable
  public String toggleOtherOffMessage() {
    return this.getString("toggle-other-off");
  }

  @Override
  @Nullable
  public String cannotUseMessage() {
    return this.getString("cannot-use-channel");
  }

  @Override
  public boolean primaryGroupOnly() {
    return this.getBoolean("primary-group-only");
  }

  @Override
  public boolean shouldCancelChatEvent() {
    return this.getBoolean("cancel-message-event");
  }

  @Override
  @NonNull
  public List<@NonNull Pattern> itemLinkPatterns() {
    final ArrayList<Pattern> itemPatterns = new ArrayList<>();

    for (final String entry : this.carbonChat.getConfig().getStringList("item-link-placeholders")) {
      itemPatterns.add(Pattern.compile(Pattern.quote(entry)));
    }

    return itemPatterns;
  }

  @Override
  @Nullable
  public Context context(@NonNull final String key) {
    final ConfigurationSection section = this.config == null ? null : this.config.getConfigurationSection("contexts");

    if (section == null) {
      final ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

      if (defaultSection == null) {
        return null;
      }

      final ConfigurationSection defaultContexts = defaultSection.getConfigurationSection("contexts");

      if (defaultContexts == null) {
        return null;
      }

      final Object value = defaultContexts.get(key);

      if (value != null) {
        return new Context(key, value);
      }

    }

    final Object value = section.get(key);

    if (value == null) {
      return null;
    }

    return new Context(key, value);
  }

  @Nullable
  @SuppressWarnings("checkstyle:MethodName")
  private String getString(@NonNull final String key) {
    if (this.config != null && this.config.contains(key)) {
      return this.config.getString(key);
    }

    final ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

    if (defaultSection != null && defaultSection.contains(key)) {
      return defaultSection.getString(key);
    }

    return null;
  }

  @NonNull
  @SuppressWarnings("checkstyle:MethodName")
  private List<@NonNull String> getStringList(@NonNull final String key) {
    if (this.config != null && this.config.contains(key)) {
      return this.config.getStringList(key);
    }

    final ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

    if (defaultSection != null && defaultSection.contains(key)) {
      return defaultSection.getStringList(key);
    }

    return Collections.emptyList();
  }

  @SuppressWarnings("checkstyle:MethodName")
  private boolean getBoolean(@NonNull final String key) {
    if (this.config != null && this.config.contains(key)) {
      return this.config.getBoolean(key);
    }

    final ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

    if (defaultSection != null && defaultSection.contains(key)) {
      return defaultSection.getBoolean(key);
    }

    return false;
  }

  @SuppressWarnings("checkstyle:MethodName")
  private double getDouble(@NonNull final String key) {
    if (this.config != null && this.config.contains(key)) {
      return this.config.getDouble(key);
    }

    final ConfigurationSection defaultSection = this.carbonChat.getConfig().getConfigurationSection("default");

    if (defaultSection != null && defaultSection.contains(key)) {
      return defaultSection.getDouble(key);
    }

    return 0;
  }

  @Override
  @NonNull
  public String key() {
    return this.key;
  }

  @Override
  @Nullable
  public String aliases() {
    final String aliases = this.getString("aliases");

    if (aliases == null) {
      return this.key();
    }

    return aliases;
  }
}

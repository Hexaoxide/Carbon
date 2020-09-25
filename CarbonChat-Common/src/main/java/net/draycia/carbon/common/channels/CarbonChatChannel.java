package net.draycia.carbon.common.channels;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.config.ChannelSettings;
import net.draycia.carbon.common.utils.ColorUtils;
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
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class CarbonChatChannel implements ChatChannel {

  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private final ChannelSettings settings;

  public CarbonChatChannel(final @NonNull CarbonChat carbonChat, @Nullable final ChannelSettings settings) {
    this.carbonChat = carbonChat;
    this.settings = settings;
  }
  
  public @NonNull ChannelSettings settings() {
    return this.settings;
  }

  @NonNull
  public CarbonChat carbonChat() {
    return this.carbonChat;
  }

  @Override
  public boolean testContext(final @NonNull ChatUser sender, final @NonNull ChatUser target) {
    final ReceiverContextEvent event = new ReceiverContextEvent(this, sender, target);

    CarbonEvents.post(event);

    return !event.cancelled();
  }

  @Override
  public boolean canPlayerUse(final @NonNull ChatUser user) {
    return user.permissible() && user.hasPermission("carbonchat.channels." + this.name() + ".use");
  }

  @Override
  @NonNull
  public List<@NonNull ChatUser> audiences() {
    final List<ChatUser> audience = new ArrayList<>();

    for (final ChatUser user : this.carbonChat.userService().onlineUsers()) {
      if (this.canPlayerSee(user, true)) {
        audience.add(user);
      }
    }

    return audience;
  }

  //  private void updateUserNickname(final @NonNull ChatUser user) {
  //    final Player player = Bukkit.getPlayer(user.uuid());
  //
  //    if (player != null) {
  //      String nickname = user.nickname();
  //
  //      if (nickname != null) {
  //        final Component component = this.carbonChat.messageProcessor().processMessage(nickname);
  //        nickname = CarbonChat.LEGACY.serialize(component);
  //
  //        player.setDisplayName(nickname);
  //
  //        if (this.carbonChat.getConfig().getBoolean("nicknames-set-tab-name")) {
  //          player.setPlayerListName(nickname);
  //        }
  //      }
  //    }
  //  }

  @Override
  @NonNull
  public Component sendMessage(final @NonNull ChatUser user, final @NonNull Collection<@NonNull ChatUser> recipients, final @NonNull String message, final boolean fromRemote) {
    //this.updateUserNickname(user);

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
      displayName = user.displayName();
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
        "server", this.carbonChat.carbonSettings().serverName(),
        "message", formatEvent.message());

      if (this.isUserSpying(user, target)) {
        final String prefix = this.carbonChat.carbonSettings().spyPrefix();

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
      "server", this.carbonChat.carbonSettings().serverName(),
      "message", consoleFormatEvent.message());

    final ChatComponentEvent consoleEvent = new ChatComponentEvent(user, null, this, consoleFormat,
      consoleFormatEvent.message());

    CarbonEvents.post(consoleEvent);

    if (user.online() && !fromRemote && this.crossServer()) {
      this.sendMessageToBungee(user.uuid(), consoleEvent.component());
    }

    return consoleEvent.component();
  }

  @Override
  @NonNull
  public Component sendMessage(final @NonNull ChatUser user, final @NonNull String message, final boolean fromRemote) {
    return this.sendMessage(user, this.audiences(), message, fromRemote);
  }

  @Nullable
  public String format(final @NonNull ChatUser user) {
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

  private boolean userHasGroup(final @NonNull ChatUser user, final @NonNull String group) {
    if (user.hasGroup(group)) {
      return true;
    }

    if (this.permissionGroupMatching()) {
      return user.hasPermission("carbonchat.group." + group);
    }

    return false;
  }

  @Nullable
  private String firstFoundUserFormat(final @NonNull ChatUser user) {
    for (final Group group : user.groups()) {
      final String groupFormat = this.format(group);

      if (groupFormat != null) {
        return groupFormat;
      }
    }

    return this.defaultFormat();
  }

  @Nullable
  private String primaryGroupFormat(final @NonNull ChatUser user) {
    final Group primaryGroup = user.primaryGroup();
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
  public String format(final @NonNull Group group) {
    return this.format(group.getName());
  }

  @Override
  @Nullable
  public String format(final @NonNull String group) {
    return this.settings().formats().get("formats." + group);
  }

  private boolean isUserSpying(final @NonNull ChatUser sender, final @NonNull ChatUser target) {
    if (!this.canPlayerSee(sender, target, false)) {
      return target.channelSettings(this).spying();
    }

    return false;
  }

  @Override
  public void sendComponent(final @NonNull ChatUser player, final @NonNull Component component) {
    for (final ChatUser user : this.audiences()) {
      if (!user.ignoringUser(player)) {
        user.sendMessage(component);
      }
    }
  }

  public void sendMessageToBungee(final @NonNull UUID uuid, final @NonNull Component component) {
    this.carbonChat.messageService().sendMessage("channel-component", uuid, byteArray -> {
      byteArray.writeUTF(this.key());
      byteArray.writeUTF(this.carbonChat.messageProcessor().audiences().gsonSerializer().serialize(component));
    });
  }

  @Override
  public boolean canPlayerSee(final @NonNull ChatUser target, final boolean checkSpying) {
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

  public boolean canPlayerSee(final @NonNull ChatUser sender, final @NonNull ChatUser target, final boolean checkSpying) {

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
  public TextColor channelColor(final @NonNull ChatUser user) {
    final TextColor userColor = user.channelSettings(this).color();

    if (userColor != null) {
      return userColor;
    }

    final String input = this.settings().color();

    final TextColor color = ColorUtils.parseColor(user, input);

    if (color == null) {
      if (this.carbonChat.carbonSettings().showTips()) {
        this.carbonChat.logger().error("Tip: Channel color found (" + input + ") is invalid!");
        this.carbonChat.logger().error("Falling back to #FFFFFF");
      }

      return NamedTextColor.WHITE;
    }

    return color;
  }

  @Nullable
  public String defaultFormatName() {
    return this.settings().defaultFormatName();
  }

  @Override
  public boolean isDefault() {
    return this.settings().isDefault();
  }

  @Override
  public boolean ignorable() {
    return this.settings().ignorable();
  }

  @Override
  public boolean crossServer() {
    return this.settings().crossServer();
  }

  @Override
  public boolean honorsRecipientList() {
    return this.settings().honorsRecipientList();
  }

  @Override
  public boolean permissionGroupMatching() {
    return this.settings().permissionGroupMatching();
  }

  @Override
  @NonNull
  public List<@NonNull String> groupOverrides() {
    return this.settings().groupOverrides();
  }

  @Override
  @NonNull
  public String name() {
    return this.settings().name();
  }

  @Override
  @Nullable
  public String messagePrefix() {
    return this.settings().messagePrefix();
  }

  @Override
  @Nullable
  public String switchMessage() {
    return this.settings().switchMessage();
  }

  @Override
  @Nullable
  public String switchOtherMessage() {
    return this.settings().switchOtherMessage();
  }

  @Override
  @Nullable
  public String switchFailureMessage() {
    return this.settings().switchFailureMessage();
  }

  @Override
  @Nullable
  public String cannotIgnoreMessage() {
    return this.settings().cannotIgnoreMessage();
  }

  @Override
  @Nullable
  public String toggleOffMessage() {
    return this.settings().toggleOffMessage();
  }

  @Override
  @Nullable
  public String toggleOnMessage() {
    return this.settings().toggleOnMessage();
  }

  @Override
  @Nullable
  public String toggleOtherOnMessage() {
    return this.settings().toggleOtherOnMessage();
  }

  @Override
  @Nullable
  public String toggleOtherOffMessage() {
    return this.settings().toggleOtherOffMessage();
  }

  @Override
  @Nullable
  public String cannotUseMessage() {
    return this.settings().cannotUseMessage();
  }

  @Override
  public boolean primaryGroupOnly() {
    return this.settings().primaryGroupOnly();
  }

  @Override
  public boolean shouldCancelChatEvent() {
    return this.settings().shouldCancelChatEvent();
  }

  @Override
  @NonNull
  public List<@NonNull Pattern> itemLinkPatterns() {
    return this.carbonChat.carbonSettings().itemLinkPatterns();
  }

  @Override
  @Nullable
  public List<String> aliases() {
    return this.settings().aliases();
  }

  @Override
  @Nullable
  public Context context(final @NonNull String key) {
    return this.settings().contexts().get(key);
  }

  @Override
  @NonNull
  public String key() {
    return this.settings().key();
  }

}

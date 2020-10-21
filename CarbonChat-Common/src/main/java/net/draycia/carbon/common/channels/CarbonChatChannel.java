package net.draycia.carbon.common.channels;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.config.ChannelOptions;
import net.draycia.carbon.api.users.ConsoleUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.utils.ColorUtils;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatComponentEvent;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.draycia.carbon.api.events.MessageContextEvent;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.draycia.carbon.api.events.ReceiverContextEvent;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.Context;
import net.kyori.adventure.identity.Identity;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class CarbonChatChannel implements TextChannel {

  private final @NonNull CarbonChat carbonChat;

  private final @NonNull ChannelOptions options;

  public CarbonChatChannel(final @NonNull CarbonChat carbonChat, final @NonNull ChannelOptions options) {
    this.carbonChat = carbonChat;
    this.options = options;
  }
  
  public @NonNull ChannelOptions options() {
    return this.options;
  }

  public @NonNull CarbonChat carbonChat() {
    return this.carbonChat;
  }

  @Override
  public boolean testContext(final @NonNull PlayerUser sender, final @NonNull PlayerUser target) {
    final ReceiverContextEvent event = new ReceiverContextEvent(this, sender, target);

    CarbonEvents.post(event);

    return !event.cancelled();
  }

  @Override
  public boolean canPlayerUse(final @NonNull PlayerUser user) {
    return user.hasPermission("carbonchat.channels." + this.name() + ".use");
  }

  @Override
  public @NonNull List<@NonNull PlayerUser> audiences() {
    final List<PlayerUser> audience = new ArrayList<>();

    for (final PlayerUser user : this.carbonChat.userService().onlineUsers()) {
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
  public @NonNull Map<CarbonUser, Component> parseMessage(final @NonNull PlayerUser user, final @NonNull Collection<@NonNull PlayerUser> recipients, final @NonNull String message, final boolean fromRemote) {
    //this.updateUserNickname(user);

    final MessageContextEvent event = new MessageContextEvent(this, user);

    CarbonEvents.post(event);

    if (event.cancelled()) {
      return Collections.emptyMap();
    }

    // Get player's formatting
    final String messageFormat = this.format(user);

    // Call custom chat event
    final PreChatFormatEvent preFormatEvent = new PreChatFormatEvent(user, this, messageFormat, message);

    CarbonEvents.post(preFormatEvent);

    // Return if cancelled or message is emptied

    if (preFormatEvent.cancelled() || preFormatEvent.message().trim().isEmpty()) {
      return Collections.emptyMap();
    }

    String displayName = user.nickname();

    final Map<CarbonUser, Component> users = new HashMap<>();

    // Iterate through players who should receive messages in this channel
    for (final PlayerUser target : recipients) {
      // Call second format event. Used for relational stuff (placeholders etc)
      final ChatFormatEvent formatEvent = new ChatFormatEvent(user, target, this, preFormatEvent.format(), preFormatEvent.message());

      CarbonEvents.post(formatEvent);

      // Again, return if cancelled or message is emptied
      if (formatEvent.cancelled() || formatEvent.message().trim().isEmpty()) {
        continue;
      }

      final TextColor targetColor = this.channelColor(target);

      TextComponent formatComponent = (TextComponent) this.carbonChat.messageProcessor().processMessage(formatEvent.format(),
        "displayname", displayName,
        "color", "<" + targetColor.asHexString() + ">",
        "phase", Long.toString(System.currentTimeMillis() % 25),
        "message", formatEvent.message());

      if (this.isUserSpying(user, target)) {
        final String prefix = this.carbonChat.carbonSettings().spyPrefix();

        formatComponent = (TextComponent) MiniMessage.get().parse(prefix, "color",
          targetColor.asHexString()).append(formatComponent);
      }

      final ChatComponentEvent newEvent = new ChatComponentEvent(user, target, this, formatComponent,
        formatEvent.message());

      CarbonEvents.post(newEvent);

      final PlayerUser targetUser = newEvent.target();

      if (targetUser != null) {
        users.put(targetUser, newEvent.component());
      }
    }

    // TODO: ChatFormatEvent ConsoleUser support for target, specify ConsoleUser as target
    final ChatFormatEvent consoleFormatEvent = new ChatFormatEvent(user, null, this, preFormatEvent.format(),
      preFormatEvent.message());

    CarbonEvents.post(consoleFormatEvent);

    final TextColor targetColor = this.channelColor(user);

    final TextComponent consoleFormat = (TextComponent) this.carbonChat.messageProcessor().processMessage(consoleFormatEvent.format(),
      "displayname", displayName,
      "color", "<" + targetColor.asHexString() + ">",
      "phase", Long.toString(System.currentTimeMillis() % 25),
      "message", consoleFormatEvent.message());

    final ChatComponentEvent consoleEvent = new ChatComponentEvent(user, null, this, consoleFormat,
      consoleFormatEvent.message());

    CarbonEvents.post(consoleEvent);

    if (!consoleEvent.cancelled()) {
      final ConsoleUser consoleUser = this.carbonChat.userService().consoleUser();

      if (consoleUser != null) {
        users.put(consoleUser, consoleEvent.component());
      }
    }

    if (user.online() && !fromRemote && this.crossServer()) {
      this.sendMessageToBungee(user.uuid(), consoleEvent.component());
    }

    return users;
  }

  @Override
  public void sendComponents(final @NonNull Identity identity,
                             final @NonNull Map<? extends CarbonUser, Component> components) {
    for (final Map.Entry<? extends CarbonUser, Component> entry : components.entrySet()) {
      if (entry.getValue().equals(Component.empty())) {
        continue;
      }

      entry.getKey().sendMessage(identity, entry.getValue());
    }
  }

  @Override
  public void sendComponentsAndLog(final @NonNull Identity identity,
                                   final @NonNull Map<? extends CarbonUser, Component> components) {
    for (final Map.Entry<? extends CarbonUser, Component> entry : components.entrySet()) {
      if (entry.getValue().equals(Component.empty())) {
        continue;
      }

      entry.getKey().sendMessage(identity, entry.getValue());

      if (entry instanceof ConsoleUser) {
        this.carbonChat.messageProcessor().audiences().console()
          .sendMessage(identity, entry.getValue());
      }
    }
  }

  @Override
  public @NonNull Map<CarbonUser, Component> parseMessage(final @NonNull PlayerUser user, final @NonNull String message, final boolean fromRemote) {
    return this.parseMessage(user, this.audiences(), message, fromRemote);
  }

  public @NonNull String format(final @NonNull PlayerUser user) {
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

  private boolean userHasGroup(final @NonNull PlayerUser user, final @NonNull String group) {
    if (user.hasGroup(group)) {
      return true;
    }

    if (this.permissionGroupMatching()) {
      return user.hasPermission("carbonchat.group." + group);
    }

    return false;
  }

  private @NonNull String firstFoundUserFormat(final @NonNull PlayerUser user) {
    for (final Group group : user.groups()) {
      final String groupFormat = this.format(group);

      if (groupFormat != null) {
        return groupFormat;
      }
    }

    return this.defaultFormat();
  }

  private @NonNull String primaryGroupFormat(final @NonNull PlayerUser user) {
    final Group primaryGroup = user.primaryGroup();

    if (primaryGroup == null) {
      return this.defaultFormat();
    }

    final String primaryGroupFormat = this.format(primaryGroup);

    if (primaryGroupFormat != null) {
      return primaryGroupFormat;
    }

    return this.defaultFormat();
  }

  private @NonNull String defaultFormat() {
    final String defaultFormatName = this.defaultFormatName();

    if (defaultFormatName == null) {
      return "<<displayname>> <message>";
    }

    final String defaultFormat = this.format(defaultFormatName);

    if (defaultFormat == null) {
      return "<<displayname>> <message>";
    }

    return defaultFormat;
  }

  @Override
  public @Nullable String format(final @NonNull Group group) {
    return this.format(group.getName());
  }

  @Override
  public @Nullable String format(final @NonNull String group) {
    return this.options().format(group);
  }

  private boolean isUserSpying(final @NonNull PlayerUser sender, final @NonNull PlayerUser target) {
    if (!this.canPlayerSee(sender, target, false)) {
      return target.channelSettings(this).spying();
    }

    return false;
  }

  @Override
  public void sendComponent(final @NonNull PlayerUser player, final @NonNull Component component) {
    for (final PlayerUser user : this.audiences()) {
      if (!user.ignoringUser(player)) {
        user.sendMessage(player.identity(), component);
      }
    }
  }

  public void sendMessageToBungee(final @NonNull UUID uuid, final @NonNull Component component) {
    this.carbonChat.messageService().sendMessage("channel-component", uuid, byteArray -> {
      byteArray.writeUTF(this.key());
      byteArray.writeUTF(this.carbonChat.gsonSerializer().serialize(component));
    });
  }

  @Override
  public boolean canPlayerSee(final @NonNull PlayerUser target, final boolean checkSpying) {
    if (checkSpying && target.hasPermission("carbonchat.spy." + this.name())) {
      if (target.channelSettings(this).spying()) {
        return true;
      }
    }

    if (!(target.hasPermission("carbonchat.channels." + this.name() + ".see"))) {
      return false;
    }

    if (this.ignorable()) {
      return !target.channelSettings(this).ignored();
    }

    return true;
  }

  public boolean canPlayerSee(final @NonNull PlayerUser sender, final @NonNull PlayerUser target, final boolean checkSpying) {

    if (!this.canPlayerSee(target, checkSpying)) {
      return false;
    }

    if (this.ignorable()) {
      return !target.ignoringUser(sender) || target.hasPermission("carbonchat.ignore.exempt");
    }

    return true;
  }

  @Override
  public @NonNull TextColor channelColor(final @NonNull CarbonUser user) {
    if (user instanceof PlayerUser) {
      final TextColor userColor = ((PlayerUser) user).channelSettings(this).color();

      if (userColor != null) {
        return userColor;
      }
    }

    final String input = this.options().color();

    if (input != null) {
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

    return NamedTextColor.WHITE;
  }

  public @Nullable String defaultFormatName() {
    return this.options().defaultFormatName();
  }

  @Override
  public boolean isDefault() {
    return this.options().isDefault();
  }

  @Override
  public boolean ignorable() {
    return this.options().ignorable();
  }

  @Override
  public boolean crossServer() {
    return this.options().crossServer();
  }

  @Override
  public boolean honorsRecipientList() {
    return this.options().honorsRecipientList();
  }

  @Override
  public boolean permissionGroupMatching() {
    return this.options().permissionGroupMatching();
  }

  @Override
  public @NonNull List<@NonNull String> groupOverrides() {
    return this.options().groupOverrides();
  }

  @Override
  public @NonNull String name() {
    return this.options().name();
  }

  @Override
  public @Nullable String messagePrefix() {
    return this.options().messagePrefix();
  }

  @Override
  public @Nullable String switchMessage() {
    return this.options().switchMessage();
  }

  @Override
  public @Nullable String switchOtherMessage() {
    return this.options().switchOtherMessage();
  }

  @Override
  public @Nullable String switchFailureMessage() {
    return this.options().switchFailureMessage();
  }

  @Override
  public @Nullable String cannotIgnoreMessage() {
    return this.options().cannotIgnoreMessage();
  }

  @Override
  public @Nullable String toggleOffMessage() {
    return this.options().toggleOffMessage();
  }

  @Override
  public @Nullable String toggleOnMessage() {
    return this.options().toggleOnMessage();
  }

  @Override
  public @Nullable String toggleOtherOnMessage() {
    return this.options().toggleOtherOnMessage();
  }

  @Override
  public @Nullable String toggleOtherOffMessage() {
    return this.options().toggleOtherOffMessage();
  }

  @Override
  public @Nullable String cannotUseMessage() {
    return this.options().cannotUseMessage();
  }

  @Override
  public boolean primaryGroupOnly() {
    return this.options().primaryGroupOnly();
  }

  //  @Override
  //  public boolean shouldCancelChatEvent() {
  //    return this.options().shouldCancelChatEvent();
  //  }

  @Override
  public @NonNull List<@NonNull Pattern> itemLinkPatterns() {
    return this.carbonChat.carbonSettings().itemLinkPatterns();
  }

  @Override
  public @Nullable List<String> aliases() {
    return this.options().aliases();
  }

  @Override
  public @Nullable Context context(final @NonNull String key) {
    return this.options().context(key);
  }

  @Override
  public @NonNull String key() {
    return this.options().key();
  }

}

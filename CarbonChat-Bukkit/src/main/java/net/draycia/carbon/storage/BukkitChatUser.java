package net.draycia.carbon.storage;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.users.UserChannelSettings;
import net.draycia.carbon.util.FunctionalityConstants;
import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChannelSwitchEvent;
import net.draycia.carbon.api.events.PrivateMessageEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BukkitChatUser implements ChatUser, ForwardingAudience {

  private final @NonNull transient CarbonChatBukkit carbonChat;
  private final @NonNull Map<@NonNull String, @NonNull SimpleUserChannelSettings> channelSettings = new HashMap<>();
  private final @NonNull List<@NonNull UUID> ignoredUsers = new ArrayList<>();
  private @MonotonicNonNull UUID uuid;
  private boolean muted = false;
  private boolean shadowMuted = false;
  private boolean spyingWhispers = false;
  private @Nullable String nickname = null;
  private @Nullable UUID replyTarget = null;

  private @Nullable String selectedChannelKey = null;
  private transient @Nullable ChatChannel selectedChannel = null;

  public BukkitChatUser() {
    this.carbonChat = (CarbonChatBukkit) Bukkit.getPluginManager().getPlugin("CarbonChat-Bukkit");
  }

  public BukkitChatUser(final @NonNull UUID uuid) {
    this();
    this.uuid = uuid;
  }

  @Override
  @NonNull
  public Iterable<@NonNull ? extends Audience> audiences() {
    return Collections.singleton(this.carbonChat.messageProcessor().audiences().player(this.uuid()));
  }

  @Override
  public boolean online() {
    return Bukkit.getPlayer(this.uuid()) != null;
  }

  @Override
  @NonNull
  public UUID uuid() {
    return this.uuid;
  }

  @Override
  public @Nullable String nickname() {
    return this.nickname;
  }

  @Override
  public boolean permissible() {
    return Bukkit.getPlayer(this.uuid()) != null;
  }

  @Override
  public boolean hasPermission(final @NonNull String permission) {
    return LuckPermsProvider.get().getUserManager().getUser(this.uuid())
      .getCachedData().getPermissionData().checkPermission(permission).asBoolean();
  }

  @Override
  public void nickname(@Nullable String newNickname, final boolean fromRemote) {
    this.nickname = newNickname;

    final OfflinePlayer player = Bukkit.getOfflinePlayer(this.uuid());

    if (player.isOnline()) {
      if (newNickname != null) {
        final Component component = this.carbonChat.messageProcessor().processMessage(this.nickname);
        newNickname = CarbonChatBukkit.LEGACY.serialize(component);
      }

      final Player onlinePlayer = player.getPlayer();

      onlinePlayer.setDisplayName(newNickname);

      //      if (this.carbonChat.getConfig().getBoolean("nicknames-set-tab-name")) {
      //        onlinePlayer.setPlayerListName(newNickname);
      //      }
    }

    if (!fromRemote) {
      if (this.nickname == null) {
        this.carbonChat.messageManager().sendMessage("nickname-reset", this.uuid(), byteArray -> {
        });
      } else {
        this.carbonChat.messageManager().sendMessage("nickname", this.uuid(), byteArray -> {
          byteArray.writeUTF(this.nickname);
        });
      }
    }
  }

  @Override
  public @Nullable String displayName() {
    final Player player = Bukkit.getPlayer(this.uuid());

    if (player != null) {
      return player.getDisplayName();
    }

    return this.name();
  }

  public void displayName(final @Nullable String displayName) {
    final Player player = Bukkit.getPlayer(this.uuid());

    if (player != null) {
      player.setDisplayName(displayName);
    }
  }

  public @NonNull String name() {
    return Objects.requireNonNull(Bukkit.getOfflinePlayer(this.uuid()).getName());
  }

  @Override
  public @Nullable ChatChannel selectedChannel() {
    if (this.selectedChannel != null) {
      return this.selectedChannel;
    }

    if (this.selectedChannelKey != null) {
      return this.carbonChat.channelRegistry().channelOrDefault(this.selectedChannelKey);
    }

    return null;
  }

  @Override
  public void selectedChannel(final @NonNull ChatChannel chatChannel, final boolean fromRemote) {
    final String failureMessage = chatChannel.switchFailureMessage();

    final ChannelSwitchEvent event = new ChannelSwitchEvent(chatChannel, this, failureMessage);

    CarbonEvents.post(event);

    if (event.cancelled()) {
      this.sendMessage(this.carbonChat.messageProcessor().processMessage(event.failureMessage(),
        "channel", chatChannel.name()));

      return;
    }

    this.selectedChannelKey = chatChannel.key();
    this.selectedChannel = chatChannel;

    if (!fromRemote) {
      this.carbonChat.messageManager().sendMessage("selected-channel", this.uuid(), byteArray -> {
        byteArray.writeUTF(chatChannel.key());
      });
    }

    final OfflinePlayer player = Bukkit.getOfflinePlayer(this.uuid());

    if (player.isOnline()) {
      this.sendMessage(this.carbonChat.messageProcessor().processMessage(chatChannel.switchMessage(),
        "br", "\n",
        "color", "<" + chatChannel.channelColor(this).toString() + ">",
        "channel", chatChannel.name()));
    }
  }

  @Override
  public void clearSelectedChannel() {
    this.selectedChannel(this.carbonChat.channelRegistry().defaultChannel());
  }

  @Override
  public boolean ignoringUser(final @NonNull UUID uuid) {
    return this.ignoredUsers.contains(uuid);
  }

  @Override
  public void ignoringUser(final @NonNull UUID uuid, final boolean ignoring, final boolean fromRemote) {
    if (ignoring) {
      this.ignoredUsers.add(uuid);
    } else {
      this.ignoredUsers.remove(uuid);
    }

    if (!fromRemote) {
      this.carbonChat.messageManager().sendMessage("ignoring-user", this.uuid(), byteArray -> {
        byteArray.writeLong(uuid.getMostSignificantBits());
        byteArray.writeLong(uuid.getLeastSignificantBits());
        byteArray.writeBoolean(ignoring);
      });
    }
  }

  @Override
  @NonNull
  public Iterable<@NonNull UUID> ignoredUsers() {
    return this.ignoredUsers;
  }

  private transient final LuckPerms luckPerms = LuckPermsProvider.get();

  @Override
  public boolean hasGroup(final @NonNull String groupName) {
    return this.hasGroup(this.luckPerms.getGroupManager().getGroup(groupName));
  }

  @Override
  public boolean hasGroup(final @NonNull Group group) {
    return this.groups().contains(group);
  }

  @Override
  public @NonNull Collection<@NonNull Group> groups() {
    final User user = this.luckPerms.getUserManager().getUser(this.uuid());

    return user.getInheritedGroups(QueryOptions.nonContextual());
  }

  @Override
  public @Nullable Group primaryGroup() {
    final User user = this.luckPerms.getUserManager().getUser(this.uuid());

    return this.luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
  }

  @Override
  @NonNull
  public Iterable<@NonNull ChatUser> ignoredChatUsers() {
    final List<ChatUser> users = new ArrayList<>();

    for (final UUID uuid : this.ignoredUsers) {
      users.add(this.carbonChat.userService().wrap(uuid));
    }

    return users;
  }

  @Override
  public void muted(final boolean muted, final boolean fromRemote) {
    this.muted = muted;

    if (!fromRemote) {
      this.carbonChat.messageManager().sendMessage("muted", this.uuid(), byteArray -> {
        byteArray.writeBoolean(muted);
      });
    }
  }

  @Override
  public boolean muted() {
    return this.muted;
  }

  @Override
  public void shadowMuted(final boolean shadowMuted, final boolean fromRemote) {
    this.shadowMuted = shadowMuted;

    if (!fromRemote) {
      this.carbonChat.messageManager().sendMessage("shadow-muted", this.uuid(), byteArray -> {
        byteArray.writeBoolean(shadowMuted);
      });
    }
  }

  @Override
  public boolean shadowMuted() {
    return this.shadowMuted;
  }

  @Override
  public @Nullable UUID replyTarget() {
    return this.replyTarget;
  }

  @Override
  public void replyTarget(final @Nullable UUID target, final boolean fromRemote) {
    this.replyTarget = target;

    if (!fromRemote) {
      this.carbonChat.messageManager().sendMessage("reply-target", this.uuid(), byteArray -> {
        byteArray.writeLong(target.getMostSignificantBits());
        byteArray.writeLong(target.getLeastSignificantBits());
      });
    }
  }

  @Override
  @NonNull
  public UserChannelSettings channelSettings(final @NonNull ChatChannel channel) {
    return this.channelSettings.computeIfAbsent(channel.key(), name -> {
      return new SimpleUserChannelSettings(this.uuid, channel.key());
    });
  }

  @NonNull
  public Map<@NonNull String, @NonNull ? extends UserChannelSettings> channelSettings() {
    return this.channelSettings;
  }

  @Override
  public void spyingWhispers(final boolean spyingWhispers, final boolean fromRemote) {
    this.spyingWhispers = spyingWhispers;

    if (!fromRemote) {
      this.carbonChat.messageManager().sendMessage("spying-whispers", this.uuid(), byteArray -> {
        byteArray.writeBoolean(spyingWhispers);
      });
    }
  }

  @Override
  public boolean spyingWhispers() {
    return this.spyingWhispers;
  }

  @Override
  public void sendMessage(final @NonNull ChatUser sender, final @NonNull String message) {
    if (this.ignoringUser(sender) || sender.ignoringUser(this)) {
      return;
    }

    final String toPlayerFormat = this.carbonChat.carbonSettings().whisperOptions().senderFormat();
    final String fromPlayerFormat = this.carbonChat.carbonSettings().whisperOptions().receiverFormat();

    final OfflinePlayer offlineSender = Bukkit.getOfflinePlayer(sender.uuid());
    String senderName = offlineSender.getName();
    final String senderOfflineName = senderName;

    if (offlineSender.isOnline()) {
      senderName = offlineSender.getPlayer().getDisplayName();
    }

    final OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(this.uuid());
    String targetName = offlineTarget.getName();
    final String targetOfflineName = targetName;

    if (offlineTarget.isOnline()) {
      targetName = offlineTarget.getPlayer().getDisplayName();
    }

    final Component toPlayerComponent = this.carbonChat.messageProcessor().processMessage(toPlayerFormat, "br", "\n",
      "message", message,
      "targetname", targetOfflineName, "sendername", senderOfflineName,
      "target", targetName, "sender", senderName);

    final Component fromPlayerComponent = this.carbonChat.messageProcessor().processMessage(fromPlayerFormat, "br", "\n",
      "message", message,
      "targetname", targetOfflineName, "sendername", senderOfflineName,
      "target", targetName, "sender", senderName);

    final PrivateMessageEvent event = new PrivateMessageEvent(sender, this, toPlayerComponent, fromPlayerComponent, message);

    CarbonEvents.post(event);

    if (event.cancelled()) {
      return;
    }

    if (offlineTarget.isOnline()) {
      if (offlineSender.isOnline()) {
        sender.sendMessage(toPlayerComponent);

        if (sender.shadowMuted()) {
          return;
        }

        this.sendMessage(fromPlayerComponent);

        sender.replyTarget(this);
        this.replyTarget(sender);
      }
    } else if (offlineSender.isOnline()) {
      final String targetNameFinal = targetName;
      final String senderNameFinal = senderName;

      if (!FunctionalityConstants.HAS_PROXY) {
        final String playerOfflineFormat = this.carbonChat.translations().otherPlayerOffline();

        final Component playerOfflineComponent = this.carbonChat.messageProcessor().processMessage(playerOfflineFormat,
          "br", "\n",
          "message", message,
          "targetname", targetOfflineName, "sendername", senderOfflineName,
          "target", targetNameFinal, "sender", senderNameFinal);

        sender.sendMessage(playerOfflineComponent);

        return;
      }

      BungeeChannelApi.of(this.carbonChat).getPlayerList("ALL").thenAccept(list -> {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(this.uuid());

        if (!list.contains(player.getName())) {
          final String playerOfflineFormat = this.carbonChat.translations().otherPlayerOffline();

          final Component playerOfflineComponent = this.carbonChat.messageProcessor().processMessage(playerOfflineFormat,
            "br", "\n",
            "message", message,
            "targetname", targetOfflineName, "sendername", senderOfflineName,
            "target", targetNameFinal, "sender", senderNameFinal);

          sender.sendMessage(playerOfflineComponent);

          return;
        }

        sender.sendMessage(toPlayerComponent);

        this.carbonChat.messageManager().sendMessage("whisper-component", sender.uuid(), byteArray -> {
          byteArray.writeLong(this.uuid().getMostSignificantBits());
          byteArray.writeLong(this.uuid().getLeastSignificantBits());
          byteArray.writeUTF(this.carbonChat.gsonSerializer().serialize(fromPlayerComponent));
        });
      });
    }

    for (final Player player : Bukkit.getOnlinePlayers()) {
      final ChatUser user = this.carbonChat.userService().wrap(player.getUniqueId());

      if (!user.spyingWhispers()) {
        continue;
      }

      if (user.uuid().equals(sender.uuid()) || user.uuid().equals(this.uuid())) {
        continue;
      }

      user.sendMessage(this.carbonChat.messageProcessor().processMessage(this.carbonChat.translations().spyWhispers(),
        "br", "\n", "message", message,
        "targetname", targetOfflineName, "sendername", senderOfflineName,
        "target", targetName, "sender", senderName));
    }
  }
}

package net.draycia.carbon.storage.impl;

import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChannelSwitchEvent;
import net.draycia.carbon.events.api.PrivateMessageEvent;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserChannelSettings;
import net.draycia.carbon.util.FunctionalityConstants;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.craftbukkit.BukkitComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CarbonChatUser implements ChatUser, ForwardingAudience {

  @NonNull
  private final transient CarbonChat carbonChat;
  @NonNull
  private final Map<@NonNull String, @NonNull SimpleUserChannelSettings> channelSettings = new HashMap<>();
  @NonNull
  private final List<@NonNull UUID> ignoredUsers = new ArrayList<>();
  @MonotonicNonNull // @NonNull but not initialised in all constructors.
  private UUID uuid;
  @Nullable
  private String selectedChannel = null;
  private boolean muted = false;
  private boolean shadowMuted = false;
  private boolean spyingWhispers = false;

  @Nullable
  private String nickname = null;

  @Nullable
  private transient UUID replyTarget = null;

  public CarbonChatUser() {
    this.carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");
  }

  public CarbonChatUser(@NonNull final UUID uuid) {
    this();
    this.uuid = uuid;
  }

  @Override
  @NonNull
  public Iterable<@NonNull ? extends Audience> audiences() {
    return Collections.singleton(this.carbonChat.adventureManager().audiences().player(this.uuid));
  }

  @Override
  @Nullable
  public Player player() {
    return Bukkit.getPlayer(this.uuid);
  }

  @Override
  @NonNull
  public OfflinePlayer offlinePlayer() {
    return Bukkit.getOfflinePlayer(this.uuid);
  }

  @Override
  public boolean online() {
    return this.offlinePlayer().isOnline();
  }

  @Override
  @NonNull
  public UUID uuid() {
    return this.uuid;
  }

  @Override
  @Nullable
  public String nickname() {
    return this.nickname;
  }

  @Override
  public void nickname(@Nullable String newNickname, final boolean fromRemote) {
    this.nickname = newNickname;

    if (this.online()) {
      if (newNickname != null) {
        final Component component = this.carbonChat.adventureManager().processMessage(this.nickname);
        newNickname = CarbonChat.LEGACY.serialize(component);
      }

      this.player().setDisplayName(newNickname);

      if (this.carbonChat.getConfig().getBoolean("nicknames-set-tab-name")) {
        this.player().setPlayerListName(newNickname);
      }
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
  @Nullable
  public ChatChannel selectedChannel() {
    if (this.selectedChannel == null) {
      return null;
    }

    return this.carbonChat.channelManager().channelOrDefault(this.selectedChannel);
  }

  @Override
  public void selectedChannel(@NonNull final ChatChannel chatChannel, final boolean fromRemote) {
    final String failureMessage = chatChannel.switchFailureMessage();

    final ChannelSwitchEvent event = new ChannelSwitchEvent(chatChannel, this, failureMessage);

    CarbonEvents.post(event);

    if (event.cancelled()) {
      this.sendMessage(this.carbonChat.adventureManager().processMessage(event.failureMessage(),
        "channel", chatChannel.name()));

      return;
    }

    this.selectedChannel = chatChannel.key();

    if (!fromRemote) {
      this.carbonChat.messageManager().sendMessage("selected-channel", this.uuid(), byteArray -> {
        byteArray.writeUTF(chatChannel.key());
      });
    }

    if (this.online()) {
      this.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(this.player(), chatChannel.switchMessage(),
        "br", "\n",
        "color", "<" + chatChannel.channelColor(this).toString() + ">",
        "channel", chatChannel.name()));
    }
  }

  @Override
  public void clearSelectedChannel() {
    this.selectedChannel(this.carbonChat.channelManager().defaultChannel());
  }

  @Override
  public boolean ignoringUser(@NonNull final UUID uuid) {
    return this.ignoredUsers.contains(uuid);
  }

  @Override
  public void ignoringUser(@NonNull final UUID uuid, final boolean ignoring, final boolean fromRemote) {
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

  @NonNull
  public List<@NonNull UUID> ignoredUsers() {
    return this.ignoredUsers;
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
  @Nullable
  public UUID replyTarget() {
    return this.replyTarget;
  }

  @Override
  public void replyTarget(@Nullable final UUID target, final boolean fromRemote) {
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
  public UserChannelSettings channelSettings(@NonNull final ChatChannel channel) {
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
  public boolean spyingwhispers() {
    return this.spyingWhispers;
  }

  @Override
  public void sendMessage(@NonNull final ChatUser sender, @NonNull final String message) {
    if (this.ignoringUser(sender) || sender.ignoringUser(this)) {
      return;
    }

    final String toPlayerFormat = this.carbonChat.language().getString("message-to-other");
    final String fromPlayerFormat = this.carbonChat.language().getString("message-from-other");

    String senderName = sender.offlinePlayer().getName();
    final String senderOfflineName = senderName;

    String targetName = this.offlinePlayer().getName();
    final String targetOfflineName = targetName;

    if (sender.online()) {
      senderName = sender.player().getDisplayName();
    }

    if (this.online()) {
      targetName = this.player().getDisplayName();
    }

    final Component toPlayerComponent = this.carbonChat.adventureManager().processMessage(toPlayerFormat, "br", "\n",
      "message", message,
      "targetname", targetOfflineName, "sendername", senderOfflineName,
      "target", targetName, "sender", senderName);

    final Component fromPlayerComponent = this.carbonChat.adventureManager().processMessage(fromPlayerFormat, "br", "\n",
      "message", message,
      "targetname", targetOfflineName, "sendername", senderOfflineName,
      "target", targetName, "sender", senderName);

    final PrivateMessageEvent event = new PrivateMessageEvent(sender, this, toPlayerComponent, fromPlayerComponent, message);

    CarbonEvents.post(event);

    if (event.cancelled()) {
      return;
    }

    if (this.online()) {
      if (sender.online()) {
        sender.sendMessage(toPlayerComponent);

        if (sender.shadowMuted()) {
          return;
        }

        this.sendMessage(fromPlayerComponent);

        sender.replyTarget(this);
        this.replyTarget(sender);

        if (this.carbonChat.getConfig().getBoolean("pings.on-whisper")) {
          final Key key = Key.of(this.carbonChat.getConfig().getString("pings.sound"));
          final Sound.Source source = Sound.Source.valueOf(this.carbonChat.getConfig().getString("pings.source"));
          final float volume = (float) this.carbonChat.getConfig().getDouble("pings.volume");
          final float pitch = (float) this.carbonChat.getConfig().getDouble("pings.pitch");

          this.playSound(Sound.of(key, source, volume, pitch));
        }
      }
    } else if (sender.online()) {
      final String targetNameFinal = targetName;
      final String senderNameFinal = senderName;

      if (!FunctionalityConstants.HAS_PROXY) {
        final String playerOfflineFormat = this.carbonChat.language().getString("other-player-offline");

        final Component playerOfflineComponent = this.carbonChat.adventureManager().processMessage(playerOfflineFormat,
          "br", "\n",
          "message", message,
          "targetname", targetOfflineName, "sendername", senderOfflineName,
          "target", targetNameFinal, "sender", senderNameFinal);

        sender.sendMessage(playerOfflineComponent);

        return;
      }

      BungeeChannelApi.of(this.carbonChat).getPlayerList("ALL").thenAccept(list -> {
        if (!list.contains(this.offlinePlayer().getName())) {
          final String playerOfflineFormat = this.carbonChat.language().getString("other-player-offline");

          final Component playerOfflineComponent = this.carbonChat.adventureManager().processMessage(playerOfflineFormat,
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
          byteArray.writeUTF(BukkitComponentSerializer.gson().serialize(fromPlayerComponent));
        });
      });
    }

    for (final Player player : Bukkit.getOnlinePlayers()) {
      final ChatUser user = this.carbonChat.userService().wrap(player);

      if (!user.spyingwhispers()) {
        continue;
      }

      if (user.uuid().equals(sender.uuid()) || user.uuid().equals(this.uuid())) {
        continue;
      }

      user.sendMessage(this.carbonChat.adventureManager().processMessage(this.carbonChat.language().getString("spy-whispers"),
        "br", "\n", "message", message,
        "targetname", targetOfflineName, "sendername", senderOfflineName,
        "target", targetName, "sender", senderName));
    }
  }
}

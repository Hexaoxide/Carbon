package net.draycia.carbon.storage.impl;

import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.ChannelSwitchEvent;
import net.draycia.carbon.events.PrivateMessageEvent;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserChannelSettings;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
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

  public CarbonChatUser(@NonNull UUID uuid) {
    this();
    this.uuid = uuid;
  }

  @Override
  @NonNull
  public Iterable<@NonNull ? extends Audience> audiences() {
    return Collections.singleton(carbonChat.getAdventureManager().audiences().player(uuid));
  }

  @Override
  @Nullable
  public Player player() {
    return Bukkit.getPlayer(uuid);
  }

  @Override
  @NonNull
  public OfflinePlayer offlinePlayer() {
    return Bukkit.getOfflinePlayer(uuid);
  }

  @Override
  public boolean online() {
    return offlinePlayer().isOnline();
  }

  @Override
  @NonNull
  public UUID uuid() {
    return uuid;
  }

  @Override
  @Nullable
  public String nickname() {
    return nickname;
  }

  @Override
  public void nickname(@Nullable String newNickname, boolean fromRemote) {
    this.nickname = newNickname;

    if (online()) {
      if (newNickname != null) {
        Component component = this.carbonChat.getAdventureManager().processMessage(nickname);
        newNickname = CarbonChat.LEGACY.serialize(component);
      }

      this.player().setDisplayName(newNickname);

      if (carbonChat.getConfig().getBoolean("nicknames-set-tab-name")) {
        this.player().setPlayerListName(newNickname);
      }
    }

    if (!fromRemote) {
      if (nickname == null) {
        this.carbonChat.getMessageManager().sendMessage("nickname-reset", this.uuid(), (byteArray) -> {
        });
      } else {
        this.carbonChat.getMessageManager().sendMessage("nickname", this.uuid(), (byteArray) -> {
          byteArray.writeUTF(this.nickname);
        });
      }
    }
  }

  @Override
  @Nullable
  public ChatChannel selectedChannel() {
    return this.carbonChat.getChannelManager().channelOrDefault(selectedChannel);
  }

  @Override
  public void selectedChannel(@NonNull ChatChannel chatChannel, boolean fromRemote) {
    String failureMessage = chatChannel.switchFailureMessage();

    ChannelSwitchEvent event = new ChannelSwitchEvent(chatChannel, this, failureMessage);

    Bukkit.getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      sendMessage(carbonChat.getAdventureManager().processMessage(event.failureMessage(),
        "channel", chatChannel.name()));

      return;
    }

    this.selectedChannel = chatChannel.key();

    if (!fromRemote) {
      this.carbonChat.getMessageManager().sendMessage("selected-channel", this.uuid(), (byteArray) -> {
        byteArray.writeUTF(chatChannel.key());
      });
    }
  }

  @Override
  public void clearSelectedChannel() {
    selectedChannel(carbonChat.getChannelManager().defaultChannel());
  }

  @Override
  public boolean ignoringUser(@NonNull UUID uuid) {
    return ignoredUsers.contains(uuid);
  }

  @Override
  public void ignoringUser(@NonNull UUID uuid, boolean ignoring, boolean fromRemote) {
    if (ignoring) {
      ignoredUsers.add(uuid);
    } else {
      ignoredUsers.remove(uuid);
    }

    if (!fromRemote) {
      this.carbonChat.getMessageManager().sendMessage("ignoring-user", this.uuid(), (byteArray) -> {
        byteArray.writeLong(uuid.getMostSignificantBits());
        byteArray.writeLong(uuid.getLeastSignificantBits());
        byteArray.writeBoolean(ignoring);
      });
    }
  }

  @NonNull
  public List<@NonNull UUID> getIgnoredUsers() {
    return ignoredUsers;
  }

  @Override
  public void muted(boolean muted, boolean fromRemote) {
    this.muted = muted;

    if (!fromRemote) {
      this.carbonChat.getMessageManager().sendMessage("muted", this.uuid(), (byteArray) -> {
        byteArray.writeBoolean(muted);
      });
    }
  }

  @Override
  public boolean muted() {
    return muted;
  }

  @Override
  public void shadowMuted(boolean shadowMuted, boolean fromRemote) {
    this.shadowMuted = shadowMuted;

    if (!fromRemote) {
      this.carbonChat.getMessageManager().sendMessage("shadow-muted", this.uuid(), (byteArray) -> {
        byteArray.writeBoolean(shadowMuted);
      });
    }
  }

  @Override
  public boolean shadowMuted() {
    return shadowMuted;
  }

  @Override
  @Nullable
  public UUID replyTarget() {
    return replyTarget;
  }

  @Override
  public void replyTarget(@Nullable UUID target, boolean fromRemote) {
    this.replyTarget = target;

    if (!fromRemote) {
      this.carbonChat.getMessageManager().sendMessage("reply-target", this.uuid(), (byteArray) -> {
        byteArray.writeLong(target.getMostSignificantBits());
        byteArray.writeLong(target.getLeastSignificantBits());
      });
    }
  }

  @Override
  @NonNull
  public UserChannelSettings channelSettings(@NonNull ChatChannel channel) {
    return channelSettings.computeIfAbsent(channel.key(), (name) -> {
      return new SimpleUserChannelSettings(uuid, channel.key());
    });
  }

  @NonNull
  public Map<@NonNull String, @NonNull ? extends UserChannelSettings> getChannelSettings() {
    return channelSettings;
  }

  @Override
  public void spyingWhispers(boolean spyingWhispers, boolean fromRemote) {
    this.spyingWhispers = spyingWhispers;

    if (!fromRemote) {
      this.carbonChat.getMessageManager().sendMessage("spying-whispers", this.uuid(), (byteArray) -> {
        byteArray.writeBoolean(spyingWhispers);
      });
    }
  }

  @Override
  public boolean spyingwhispers() {
    return spyingWhispers;
  }

  @Override
  public void sendMessage(@NonNull ChatUser sender, @NonNull String message) {
    if (ignoringUser(sender) || sender.ignoringUser(this)) {
      return;
    }

    String toPlayerFormat = this.carbonChat.getLanguage().getString("message-to-other");
    String fromPlayerFormat = this.carbonChat.getLanguage().getString("message-from-other");

    String senderName = sender.offlinePlayer().getName();
    String senderOfflineName = senderName;

    String targetName = this.offlinePlayer().getName();
    String targetOfflineName = targetName;

    if (sender.online()) {
      senderName = sender.player().getDisplayName();
    }

    if (this.online()) {
      targetName = this.player().getDisplayName();
    }

    Component toPlayerComponent = this.carbonChat.getAdventureManager().processMessage(toPlayerFormat, "br", "\n",
      "message", message,
      "targetname", targetOfflineName, "sendername", senderOfflineName,
      "target", targetName, "sender", senderName);

    Component fromPlayerComponent = this.carbonChat.getAdventureManager().processMessage(fromPlayerFormat, "br", "\n",
      "message", message,
      "targetname", targetOfflineName, "sendername", senderOfflineName,
      "target", targetName, "sender", senderName);

    PrivateMessageEvent event = new PrivateMessageEvent(sender, this, toPlayerComponent, fromPlayerComponent, message);

    Bukkit.getPluginManager().callEvent(event);

    if (event.isCancelled()) {
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

        if (carbonChat.getConfig().getBoolean("pings.on-whisper")) {
          Key key = Key.of(carbonChat.getConfig().getString("pings.sound"));
          Sound.Source source = Sound.Source.valueOf(carbonChat.getConfig().getString("pings.source"));
          float volume = (float) this.carbonChat.getConfig().getDouble("pings.volume");
          float pitch = (float) this.carbonChat.getConfig().getDouble("pings.pitch");

          this.playSound(Sound.of(key, source, volume, pitch));
        }
      }
    } else if (sender.online()) {
      final String targetNameFinal = targetName;
      final String senderNameFinal = senderName;

      if (!carbonChat.isBungeeEnabled()) {
        String playerOfflineFormat = this.carbonChat.getLanguage().getString("other-player-offline");

        Component playerOfflineComponent = this.carbonChat.getAdventureManager().processMessage(playerOfflineFormat,
          "br", "\n",
          "message", message,
          "targetname", targetOfflineName, "sendername", senderOfflineName,
          "target", targetNameFinal, "sender", senderNameFinal);

        sender.sendMessage(playerOfflineComponent);

        return;
      }

      BungeeChannelApi.of(carbonChat).getPlayerList("ALL").thenAccept(list -> {
        if (!list.contains(this.offlinePlayer().getName())) {
          String playerOfflineFormat = this.carbonChat.getLanguage().getString("other-player-offline");

          Component playerOfflineComponent = this.carbonChat.getAdventureManager().processMessage(playerOfflineFormat,
            "br", "\n",
            "message", message,
            "targetname", targetOfflineName, "sendername", senderOfflineName,
            "target", targetNameFinal, "sender", senderNameFinal);

          sender.sendMessage(playerOfflineComponent);

          return;
        }

        sender.sendMessage(toPlayerComponent);

        this.carbonChat.getMessageManager().sendMessage("whisper-component", sender.uuid(), (byteArray) -> {
          byteArray.writeLong(this.uuid().getMostSignificantBits());
          byteArray.writeLong(this.uuid().getLeastSignificantBits());
          byteArray.writeUTF(carbonChat.getAdventureManager().audiences().gsonSerializer().serialize(fromPlayerComponent));
        });
      });
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      ChatUser user = this.carbonChat.getUserService().wrap(player);

      if (!user.spyingwhispers()) {
        continue;
      }

      if (user.uuid().equals(sender.uuid()) || user.uuid().equals(uuid())) {
        continue;
      }

      user.sendMessage(carbonChat.getAdventureManager().processMessage(carbonChat.getLanguage().getString("spy-whispers"),
        "br", "\n", "message", message,
        "targetname", targetOfflineName, "sendername", senderOfflineName,
        "target", targetName, "sender", senderName));
    }
  }
}

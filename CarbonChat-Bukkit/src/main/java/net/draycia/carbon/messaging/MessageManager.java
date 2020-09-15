package net.draycia.carbon.messaging;

import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.messaging.impl.BungeeMessageService;
import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.messaging.impl.EmptyMessageService;
import net.draycia.carbon.messaging.impl.RedisMessageService;
import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.function.Consumer;

public class MessageManager {

  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private final MessageService messageService;

  @NonNull
  private final GsonComponentSerializer gsonSerializer;

  public MessageManager(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.gsonSerializer = this.carbonChat.adventureManager().audiences().gsonSerializer();

    String messageSystem = this.carbonChat.getConfig().getString("message-system", "none");

    if (messageSystem == null) {
      messageSystem = "none";
    }

    switch (messageSystem.toLowerCase()) {
      case "bungee":
        this.carbonChat.getLogger().info("Using Bungee Plugin Messaging for message forwarding!");
        this.messageService = new BungeeMessageService(this.carbonChat);
        break;
      case "redis":
        this.carbonChat.getLogger().info("Using Redis for message forwarding!");
        this.messageService = new RedisMessageService(this.carbonChat);
        break;
      case "none":
        this.messageService = new EmptyMessageService();
        break;
      default:
        this.carbonChat.getLogger().info("Invalid message service selected! Disabling syncing until next restart!");
        this.messageService = new EmptyMessageService();
        break;
    }

    this.registerDefaultListeners();
  }

  private void registerDefaultListeners() {
    this.messageService().registerUserMessageListener("nickname", (user, byteArray) -> {
      final String nickname = byteArray.readUTF();

      user.nickname(nickname, true);

      final Player player = Bukkit.getPlayer(user.uuid());

      if (player != null) {
        final String message = this.carbonChat.language().getString("nickname-set");

        user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player,
          message, "nickname", nickname));
      }
    });

    this.messageService().registerUserMessageListener("nickname-reset", (user, byteArray) -> {
      user.nickname(null, true);

      final Player player = Bukkit.getPlayer(user.uuid());

      if (player != null) {
        final String message = this.carbonChat.language().getString("nickname-reset");

        user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player, message));
      }
    });

    this.messageService().registerUserMessageListener("selected-channel", (user, byteArray) -> {
      user.selectedChannel(this.carbonChat.channelManager().registry().get(byteArray.readUTF()), true);
    });

    this.messageService().registerUserMessageListener("spying-whispers", (user, byteArray) -> {
      user.spyingWhispers(byteArray.readBoolean(), true);
    });

    this.messageService().registerUserMessageListener("muted", (user, byteArray) -> {
      user.muted(byteArray.readBoolean(), true);
    });

    this.messageService().registerUserMessageListener("shadow-muted", (user, byteArray) -> {
      user.shadowMuted(byteArray.readBoolean(), true);
    });

    this.messageService().registerUserMessageListener("reply-target", (user, byteArray) -> {
      user.replyTarget(new UUID(byteArray.readLong(), byteArray.readLong()), true);
    });

    this.messageService().registerUserMessageListener("ignoring-user", (user, byteArray) -> {
      user.ignoringUser(new UUID(byteArray.readLong(), byteArray.readLong()), byteArray.readBoolean(), true);
    });

    this.messageService().registerUserMessageListener("ignoring-channel", (user, byteArray) -> {
      user.channelSettings(this.carbonChat.channelManager().registry().get(byteArray.readUTF()))
        .ignoring(byteArray.readBoolean(), true);
    });

    this.messageService().registerUserMessageListener("spying-channel", (user, byteArray) -> {
      user.channelSettings(this.carbonChat.channelManager().registry().get(byteArray.readUTF()))
        .spying(byteArray.readBoolean(), true);
    });

    this.messageService().registerUserMessageListener("channel-color", (user, byteArray) -> {
      user.channelSettings(this.carbonChat.channelManager().registry().get(byteArray.readUTF()))
        .color(TextColor.fromHexString(byteArray.readUTF()), true);
    });

    this.messageService().registerUserMessageListener("channel-color-reset", (user, byteArray) -> {
      user.channelSettings(this.carbonChat.channelManager().registry().get(byteArray.readUTF()))
        .color(null, true);
    });

    this.messageService().registerUUIDMessageListener("channel-component", (uuid, byteArray) -> {
      final ChatChannel channel = this.carbonChat.channelManager().registry().get(byteArray.readUTF());
      final ChatUser user = this.carbonChat.userService().wrap(uuid);

      if (channel != null) {
        final Component component = this.gsonSerializer.deserialize(byteArray.readUTF());

        channel.sendComponent(user, component);

        this.carbonChat.adventureManager().audiences().console().sendMessage(component);
      }
    });

    this.messageService().registerUUIDMessageListener("whisper-component", (uuid, byteArray) -> {
      final UUID recipient = new UUID(byteArray.readLong(), byteArray.readLong());

      final ChatUser target = this.carbonChat.userService().wrap(recipient);
      final String message = byteArray.readUTF();

      if (!target.ignoringUser(uuid)) {
        target.replyTarget(uuid);
        target.sendMessage(this.gsonSerializer.deserialize(message));
      }
    });
  }

  @NonNull
  public MessageService messageService() {
    return this.messageService;
  }

  public void sendMessage(@NonNull final String key, @NonNull final UUID uuid,
                          @NonNull final Consumer<@NonNull ByteArrayDataOutput> consumer) {
    this.messageService().sendMessage(key, uuid, consumer);
  }

}

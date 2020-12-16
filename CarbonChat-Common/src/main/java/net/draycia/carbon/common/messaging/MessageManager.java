package net.draycia.carbon.common.messaging;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.messaging.MessageService;
import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.function.Consumer;

public class MessageManager {

  private final @NonNull CarbonChat carbonChat;

  private final @NonNull MessageService messageService;

  @SuppressWarnings("method.invocation.invalid")
  public MessageManager(final @NonNull CarbonChat carbonChat, final @NonNull MessageService messageService) {
    this.carbonChat = carbonChat;
    this.messageService = messageService;

    this.registerDefaultListeners();
  }

  private void registerDefaultListeners() {
    this.messageService().registerUserMessageListener("nickname", (user, byteArray) -> {
      final String nickname = byteArray.readUTF();
      final String message = this.carbonChat.translations().nicknameSet();

      user.nickname(nickname, true);
      user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor()
        .processMessage(message, "nickname", nickname));
    });

    this.messageService().registerUserMessageListener("nickname-reset", (user, byteArray) -> {
      final String message = this.carbonChat.translations().nicknameReset();

      user.nickname(null, true);
      user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message));
    });

    this.messageService().registerUserMessageListener("selected-channel", (user, byteArray) -> {
      final ChatChannel channel = this.carbonChat.channelRegistry().get(byteArray.readUTF());

      if (channel != null) {
        user.selectedChannel(channel, true);
      }
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

    this.messageService().registerUserMessageListener("reply-target-reset", (user, byteArray) -> {
      user.replyTarget((UUID) null, true);
    });

    this.messageService().registerUserMessageListener("ignoring-user", (user, byteArray) -> {
      user.ignoringUser(new UUID(byteArray.readLong(), byteArray.readLong()), byteArray.readBoolean(), true);
    });

    this.messageService().registerUserMessageListener("ignoring-channel", (user, byteArray) -> {
      final ChatChannel channel = this.carbonChat.channelRegistry().get(byteArray.readUTF());

      if (channel != null) {
        user.channelSettings(channel)
          .ignoring(byteArray.readBoolean(), true);
      }
    });

    this.messageService().registerUserMessageListener("spying-channel", (user, byteArray) -> {
      final ChatChannel channel = this.carbonChat.channelRegistry().get(byteArray.readUTF());

      if (channel != null) {
        user.channelSettings(channel)
          .spying(byteArray.readBoolean(), true);
      }
    });

    this.messageService().registerUserMessageListener("channel-color", (user, byteArray) -> {
      final ChatChannel channel = this.carbonChat.channelRegistry().get(byteArray.readUTF());

      if (channel != null) {
        user.channelSettings(channel)
          .color(TextColor.fromHexString(byteArray.readUTF()), true);
      }
    });

    this.messageService().registerUserMessageListener("channel-color-reset", (user, byteArray) -> {
      final ChatChannel channel = this.carbonChat.channelRegistry().get(byteArray.readUTF());

      if (channel != null) {
        user.channelSettings(channel)
          .color(null, true);
      }
    });

    this.messageService().registerUUIDMessageListener("channel-component", (uuid, byteArray) -> {
      final ChatChannel channel = this.carbonChat.channelRegistry().get(byteArray.readUTF());
      final PlayerUser user = this.carbonChat.userService().wrap(uuid);

      if (user != null && channel instanceof TextChannel) {
        final Component component = this.carbonChat.gsonSerializer().deserialize(byteArray.readUTF());

        ((TextChannel) channel).sendComponent(user, component);

        this.carbonChat.messageProcessor().audiences().console().sendMessage(Identity.identity(uuid), component);
      }
    });

    this.messageService().registerUUIDMessageListener("whisper-component", (uuid, byteArray) -> {
      final UUID recipient = new UUID(byteArray.readLong(), byteArray.readLong());

      final PlayerUser sender = this.carbonChat.userService().wrap(uuid);
      final PlayerUser target = this.carbonChat.userService().wrap(recipient);
      final String message = byteArray.readUTF();

      if (!target.ignoringUser(uuid)) {
        target.replyTarget(uuid);
        target.sendMessage(Identity.identity(uuid), this.carbonChat.gsonSerializer().deserialize(message));
      }

      for (final PlayerUser user : this.carbonChat.userService().onlineUsers()) {
        if (!user.spyingWhispers()) {
          continue;
        }

        if (user.equals(target) || user.uuid().equals(uuid)) {
          continue;
        }

        user.sendMessage(sender, this.carbonChat.messageProcessor()
          .processMessage(this.carbonChat.translations().spyWhispers(), "message", message,
            "target", target.displayName(), "sender", sender.displayName()));
      }
    });

    this.messageService().registerUserMessageListener("custom-chat-color-reset", (user, byteArray) -> {
      user.customChatColor(null, true);
    });

    this.messageService().registerUserMessageListener("custom-chat-color", (user, byteArray) -> {
      user.customChatColor(TextColor.fromHexString(byteArray.readUTF()), true);
    });
  }

  public @NonNull MessageService messageService() {
    return this.messageService;
  }

  public void sendMessage(final @NonNull String key, final @NonNull UUID uuid,
                          final @NonNull Consumer<@NonNull ByteArrayDataOutput> consumer) {
    this.messageService().sendMessage(key, uuid, consumer);
  }

}

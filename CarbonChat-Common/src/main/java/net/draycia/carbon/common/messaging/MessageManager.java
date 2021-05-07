package net.draycia.carbon.common.messaging;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.messaging.MessageService;
import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
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
  }

  public @NonNull MessageService messageService() {
    return this.messageService;
  }

  public void sendMessage(final @NonNull String key, final @NonNull UUID uuid,
                          final @NonNull Consumer<@NonNull ByteArrayDataOutput> consumer) {
    this.messageService().sendMessage(key, uuid, consumer);
  }

}

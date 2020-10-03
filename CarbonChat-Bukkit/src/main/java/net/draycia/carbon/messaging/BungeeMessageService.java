package net.draycia.carbon.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.api.users.ChatUser;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BungeeMessageService implements MessageService {

  private final @NonNull CarbonChatBukkit carbonChat;

  private final @NonNull BungeeChannelApi api;

  private final @NonNull Map<@NonNull String, @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput>> userLoadedListeners = new HashMap<>();

  private final @NonNull Map<@NonNull String, @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput>> userNotLoadedListeners = new HashMap<>();

  private final @NonNull UUID serverUUID = UUID.randomUUID();

  public BungeeMessageService(final @NonNull CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;

    this.carbonChat.getServer().getMessenger().registerOutgoingPluginChannel(carbonChat, "BungeeCord");

    this.api = BungeeChannelApi.of(carbonChat);

    this.api.registerForwardListener((String channel, Player player, byte[] bytes) -> {
      try {
        final ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

        // Separated out for ease of debugging.
        final long mostServer = input.readLong();
        final long leastServer = input.readLong();

        final UUID messageUUID = new UUID(mostServer, leastServer);

        if (messageUUID.equals(this.serverUUID)) {
          return;
        }

        final long mostUser = input.readLong();
        final long leastUser = input.readLong();

        final UUID userUUID = new UUID(mostUser, leastUser);

        this.receiveMessage(userUUID, channel, input);
      } catch (final IllegalStateException ignored) {
      }
    });
  }

  private void receiveMessage(final @NonNull UUID uuid, final @NonNull String key, final @NonNull ByteArrayDataInput value) {
    final ChatUser user = this.carbonChat.userService().wrapIfLoaded(uuid);

    if (user != null) {
      for (final Map.Entry<String, BiConsumer<ChatUser, ByteArrayDataInput>> listener : this.userLoadedListeners.entrySet()) {
        if (key.equals(listener.getKey())) {
          listener.getValue().accept(user, value);
        }
      }
    }

    for (final Map.Entry<String, BiConsumer<UUID, ByteArrayDataInput>> listener : this.userNotLoadedListeners.entrySet()) {
      if (key.equals(listener.getKey())) {
        listener.getValue().accept(uuid, value);
      }
    }
  }

  @Override
  public void registerUserMessageListener(final @NonNull String key, final @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput> listener) {
    this.userLoadedListeners.put(key, listener);
  }

  @Override
  public void registerUUIDMessageListener(final @NonNull String key, final @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener) {
    this.userNotLoadedListeners.put(key, listener);
  }

  @Override
  public void unregisterMessageListener(final @NonNull String key) {
    this.userLoadedListeners.remove(key);
    this.userNotLoadedListeners.remove(key);
  }

  @Override
  public void sendMessage(final @NonNull String key, final @NonNull UUID uuid, final @NonNull Consumer<ByteArrayDataOutput> consumer) {
    final ByteArrayDataOutput msg = ByteStreams.newDataOutput();

    msg.writeLong(this.serverUUID.getMostSignificantBits());
    msg.writeLong(this.serverUUID.getLeastSignificantBits());
    msg.writeLong(uuid.getMostSignificantBits());
    msg.writeLong(uuid.getLeastSignificantBits());

    consumer.accept(msg);

    this.api.forward("ALL", key, msg.toByteArray());
  }

}

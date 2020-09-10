package net.draycia.carbon.messaging.impl;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.messaging.MessageService;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BungeeMessageService implements MessageService {

  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private final BungeeChannelApi api;

  @NonNull
  private final Map<@NonNull String, @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput>> userLoadedListeners = new HashMap<>();

  @NonNull
  private final Map<@NonNull String, @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput>> userNotLoadedListeners = new HashMap<>();

  @NonNull
  private final UUID serverUUID = UUID.randomUUID();

  public BungeeMessageService(@NonNull CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    this.carbonChat.getServer().getMessenger().registerOutgoingPluginChannel(carbonChat, "BungeeCord");

    api = BungeeChannelApi.of(carbonChat);

    api.registerForwardListener((String channel, Player player, byte[] bytes) -> {
      try {
        ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

        // Separated out for ease of debugging.
        long mostServer = input.readLong();
        long leastServer = input.readLong();

        UUID messageUUID = new UUID(mostServer, leastServer);

        if (messageUUID.equals(serverUUID)) {
          return;
        }

        long mostUser = input.readLong();
        long leastUser = input.readLong();

        UUID userUUID = new UUID(mostUser, leastUser);

        this.receiveMessage(userUUID, channel, input);
      } catch (IllegalStateException ignored) {
      }
    });
  }

  private void receiveMessage(@NonNull UUID uuid, @NonNull String key, @NonNull ByteArrayDataInput value) {
    ChatUser user = this.carbonChat.getUserService().wrapIfLoaded(uuid);

    if (user != null) {
      for (Map.Entry<String, BiConsumer<ChatUser, ByteArrayDataInput>> listener : userLoadedListeners.entrySet()) {
        if (key.equals(listener.getKey())) {
          listener.getValue().accept(user, value);
        }
      }
    }

    for (Map.Entry<String, BiConsumer<UUID, ByteArrayDataInput>> listener : userNotLoadedListeners.entrySet()) {
      if (key.equals(listener.getKey())) {
        listener.getValue().accept(uuid, value);
      }
    }
  }

  @Override
  public void registerUserMessageListener(@NonNull String key, @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput> listener) {
    userLoadedListeners.put(key, listener);
  }

  @Override
  public void registerUUIDMessageListener(@NonNull String key, @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener) {
    userNotLoadedListeners.put(key, listener);
  }

  @Override
  public void unregisterMessageListener(@NonNull String key) {
    userLoadedListeners.remove(key);
    userNotLoadedListeners.remove(key);
  }

  @Override
  public void sendMessage(@NonNull String key, @NonNull UUID uuid, @NonNull Consumer<ByteArrayDataOutput> consumer) {
    ByteArrayDataOutput msg = ByteStreams.newDataOutput();

    msg.writeLong(serverUUID.getMostSignificantBits());
    msg.writeLong(serverUUID.getLeastSignificantBits());
    msg.writeLong(uuid.getMostSignificantBits());
    msg.writeLong(uuid.getLeastSignificantBits());

    consumer.accept(msg);

    api.forward("ALL", key, msg.toByteArray());
  }

}

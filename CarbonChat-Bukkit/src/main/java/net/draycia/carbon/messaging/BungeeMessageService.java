package net.draycia.carbon.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.api.users.CarbonUser;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BungeeMessageService implements MessageService {

  private @NonNull final CarbonChatBukkit carbonChat;

  private @NonNull final BungeeChannelApi api;

  private @NonNull final Map<@NonNull String, @NonNull BiConsumer<@NonNull CarbonUser, @NonNull ByteArrayDataInput>> userLoadedListeners = new HashMap<>();

  private @NonNull final Map<@NonNull String, @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput>> userNotLoadedListeners = new HashMap<>();

  private @NonNull final UUID serverUUID = UUID.randomUUID();

  public BungeeMessageService(@NonNull final CarbonChatBukkit carbonChat) {
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

  private void receiveMessage(@NonNull final UUID uuid, @NonNull final String key, @NonNull final ByteArrayDataInput value) {
    final CarbonUser user = this.carbonChat.userService().wrapIfLoaded(uuid);

    if (user != null) {
      for (final Map.Entry<String, BiConsumer<CarbonUser, ByteArrayDataInput>> listener : this.userLoadedListeners.entrySet()) {
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
  public void registerUserMessageListener(@NonNull final String key, @NonNull final BiConsumer<@NonNull CarbonUser, @NonNull ByteArrayDataInput> listener) {
    this.userLoadedListeners.put(key, listener);
  }

  @Override
  public void registerUUIDMessageListener(@NonNull final String key, @NonNull final BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener) {
    this.userNotLoadedListeners.put(key, listener);
  }

  @Override
  public void unregisterMessageListener(@NonNull final String key) {
    this.userLoadedListeners.remove(key);
    this.userNotLoadedListeners.remove(key);
  }

  @Override
  public void sendMessage(@NonNull final String key, @NonNull final UUID uuid, @NonNull final Consumer<ByteArrayDataOutput> consumer) {
    final ByteArrayDataOutput msg = ByteStreams.newDataOutput();

    msg.writeLong(this.serverUUID.getMostSignificantBits());
    msg.writeLong(this.serverUUID.getLeastSignificantBits());
    msg.writeLong(uuid.getMostSignificantBits());
    msg.writeLong(uuid.getLeastSignificantBits());

    consumer.accept(msg);

    this.api.forward("ALL", key, msg.toByteArray());
  }

}

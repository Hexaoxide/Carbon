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

    private final @NonNull CarbonChat carbonChat;
    private final BungeeChannelApi api;

    private final Map<String, BiConsumer<ChatUser, ByteArrayDataInput>> userLoadedListeners = new HashMap<>();
    private final Map<String, BiConsumer<UUID, ByteArrayDataInput>> userNotLoadedListeners = new HashMap<>();

    private final UUID serverUUID = UUID.randomUUID();

    public BungeeMessageService(@NonNull CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        carbonChat.getServer().getMessenger().registerOutgoingPluginChannel(carbonChat, "BungeeCord");

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
            } catch (IllegalStateException ignored) {}
        });
    }

    private void receiveMessage(UUID uuid, @NonNull String key, ByteArrayDataInput value) {
        ChatUser user = carbonChat.getUserService().wrapIfLoaded(uuid);

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
    public void registerUserMessageListener(String key, BiConsumer<ChatUser, ByteArrayDataInput> listener) {
        userLoadedListeners.put(key, listener);
    }

    @Override
    public void registerUUIDMessageListener(String key, BiConsumer<UUID, ByteArrayDataInput> listener) {
        userNotLoadedListeners.put(key, listener);
    }

    @Override
    public void unregisterMessageListener(String key) {
        userLoadedListeners.remove(key);
        userNotLoadedListeners.remove(key);
    }

    @Override
    public void sendMessage(String key, @NonNull UUID uuid, @NonNull Consumer<ByteArrayDataOutput> consumer) {
        ByteArrayDataOutput msg = ByteStreams.newDataOutput();

        msg.writeLong(serverUUID.getMostSignificantBits());
        msg.writeLong(serverUUID.getLeastSignificantBits());
        msg.writeLong(uuid.getMostSignificantBits());
        msg.writeLong(uuid.getLeastSignificantBits());

        consumer.accept(msg);

        api.forward("ALL", key, msg.toByteArray());
    }

}

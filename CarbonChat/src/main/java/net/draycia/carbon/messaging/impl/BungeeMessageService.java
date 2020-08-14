package net.draycia.carbon.messaging.impl;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.messaging.MessageService;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BungeeMessageService implements PluginMessageListener, MessageService {

    private final CarbonChat carbonChat;
    private final BungeeChannelApi api;

    private Map<String, BiConsumer<ChatUser, ByteArrayDataInput>> listeners = new HashMap<>();

    private final UUID serverUUID = UUID.randomUUID();

    public BungeeMessageService(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        carbonChat.getServer().getMessenger().registerOutgoingPluginChannel(carbonChat, "BungeeCord");
        carbonChat.getServer().getMessenger().registerIncomingPluginChannel(carbonChat, "BungeeCord", this);

        api = BungeeChannelApi.of(carbonChat);
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput input = ByteStreams.newDataInput(message);

        String key = input.readUTF();
        short length = input.readShort();
        byte[] msgbytes = new byte[length];
        input.readFully(msgbytes);

        ByteArrayDataInput msg = ByteStreams.newDataInput(msgbytes);

        UUID messageUUID = new UUID(msg.readLong(), msg.readLong());

        if (messageUUID.equals(serverUUID)) {
            return;
        }

        UUID uuid = new UUID(msg.readLong(), msg.readLong());

        ChatUser user = carbonChat.getUserService().wrapIfLoaded(uuid);

        if (user == null) {
            return;
        }

        this.receiveMessage(user, key, msg);
    }

    private void receiveMessage(ChatUser user, String key, ByteArrayDataInput value) {
        for (Map.Entry<String, BiConsumer<ChatUser, ByteArrayDataInput>> listener : listeners.entrySet()) {
            if (key.equals(listener.getKey())) {
                listener.getValue().accept(user, value);
            }
        }
    }

    @Override
    public void registerMessageListener(String key, BiConsumer<ChatUser, ByteArrayDataInput> listener) {
        listeners.put(key, listener);
    }

    @Override
    public void unregisterMessageListener(String key) {
        listeners.remove(key);
    }

    @Override
    public void sendMessage(String key, UUID uuid, Consumer<ByteArrayDataOutput> consumer) {
        ByteArrayDataOutput msg = ByteStreams.newDataOutput();

        msg.writeLong(serverUUID.getMostSignificantBits());
        msg.writeLong(serverUUID.getLeastSignificantBits());
        msg.writeLong(uuid.getMostSignificantBits());
        msg.writeLong(uuid.getLeastSignificantBits());

        consumer.accept(msg);

        api.forward("ALL", key, msg.toByteArray());
    }

}

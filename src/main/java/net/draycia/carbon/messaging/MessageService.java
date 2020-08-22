package net.draycia.carbon.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.storage.ChatUser;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MessageService {

    void registerUserMessageListener(String key, BiConsumer<ChatUser, ByteArrayDataInput> listener);
    void registerUUIDMessageListener(String key, BiConsumer<UUID, ByteArrayDataInput> listener);

    void unregisterMessageListener(String key);

    void sendMessage(String key, UUID uuid, Consumer<ByteArrayDataOutput> consumer);

}

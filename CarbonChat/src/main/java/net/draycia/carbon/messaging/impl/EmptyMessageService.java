package net.draycia.carbon.messaging.impl;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.messaging.MessageService;
import net.draycia.carbon.storage.ChatUser;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EmptyMessageService implements MessageService {

    @Override
    public void registerUserMessageListener(String key, BiConsumer<ChatUser, ByteArrayDataInput> listener) {
        // do nothing
    }

    @Override
    public void registerUUIDMessageListener(String key, BiConsumer<UUID, ByteArrayDataInput> listener) {
        // do nothing
    }

    @Override
    public void unregisterMessageListener(String key) {
        // do nothing
    }

    @Override
    public void sendMessage(String key, UUID uuid, Consumer<ByteArrayDataOutput> consumer) {
        // do nothing
    }

}

package net.draycia.carbon.messaging;

import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.messaging.impl.BungeeMessageService;
import net.draycia.carbon.messaging.impl.EmptyMessageService;
import net.draycia.carbon.messaging.impl.RedisMessageService;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.UUID;
import java.util.function.Consumer;

public class MessageManager {

    private final CarbonChat carbonChat;
    private final MessageService messageService;

    private final GsonComponentSerializer gsonSerializer;

    public MessageManager(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
        this.gsonSerializer = carbonChat.getAdventureManager().getAudiences().gsonSerializer();

        String messageSystem = carbonChat.getConfig().getString("message-system", "none");

        if (messageSystem == null) {
            messageSystem = "none";
        }

        switch(messageSystem.toLowerCase()) {
            case "bungee":
                carbonChat.getLogger().info("Using Bungee Plugin Messaging for message forwarding!");
                messageService = new BungeeMessageService(carbonChat);
                break;
            case "redis":
                carbonChat.getLogger().info("Using Redis for message forwarding!");
                messageService = new RedisMessageService(carbonChat);
                break;
            case "none":
                messageService = new EmptyMessageService();
                break;
            default:
                carbonChat.getLogger().info("Invalid message service selected! Disabling syncing until next restart!");
                messageService = new EmptyMessageService();
                break;
        }

        this.registerDefaultListeners();
    }

    private void registerDefaultListeners() {
        getMessageService().registerUserMessageListener("nickname", (user, byteArray) -> {
            String nickname = byteArray.readUTF();

            user.setNickname(nickname, true);

            if (user.isOnline()) {
                String message = carbonChat.getLanguage().getString("nickname-set");

                user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(user.asPlayer(),
                        message, "nickname", nickname));
            }
        });

        getMessageService().registerUserMessageListener("nickname-reset", (user, byteArray) -> {
            user.setNickname(null, true);

            if (user.isOnline()) {
                String message = carbonChat.getLanguage().getString("nickname-reset");

                user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(user.asPlayer(), message));
            }
        });

        getMessageService().registerUserMessageListener("selected-channel", (user, byteArray) -> {
            user.setSelectedChannel(carbonChat.getChannelManager().getRegistry().get(byteArray.readUTF()), true);
        });

        getMessageService().registerUserMessageListener("spying-whispers", (user, byteArray) -> {
            user.setSpyingWhispers(byteArray.readBoolean(), true);
        });

        getMessageService().registerUserMessageListener("muted", (user, byteArray) -> {
            user.setMuted(byteArray.readBoolean(), true);
        });

        getMessageService().registerUserMessageListener("shadow-muted", (user, byteArray) -> {
            user.setShadowMuted(byteArray.readBoolean(), true);
        });

        getMessageService().registerUserMessageListener("reply-target", (user, byteArray) -> {
            user.setReplyTarget(new UUID(byteArray.readLong(), byteArray.readLong()), true);
        });

        getMessageService().registerUserMessageListener("ignoring-user", (user, byteArray) -> {
            user.setIgnoringUser(new UUID(byteArray.readLong(), byteArray.readLong()), byteArray.readBoolean(), true);
        });

        getMessageService().registerUserMessageListener("ignoring-channel", (user, byteArray) -> {
            user.getChannelSettings(carbonChat.getChannelManager().getRegistry().get(byteArray.readUTF()))
                    .setIgnoring(byteArray.readBoolean(), true);
        });

        getMessageService().registerUserMessageListener("spying-channel", (user, byteArray) -> {
            user.getChannelSettings(carbonChat.getChannelManager().getRegistry().get(byteArray.readUTF()))
                    .setSpying(byteArray.readBoolean(), true);
        });

        getMessageService().registerUserMessageListener("channel-color", (user, byteArray) -> {
            user.getChannelSettings(carbonChat.getChannelManager().getRegistry().get(byteArray.readUTF()))
                    .setColor(TextColor.fromHexString(byteArray.readUTF()), true);
        });

        getMessageService().registerUserMessageListener("channel-color-reset", (user, byteArray) -> {
            user.getChannelSettings(carbonChat.getChannelManager().getRegistry().get(byteArray.readUTF()))
                    .setColor(null, true);
        });

        getMessageService().registerUUIDMessageListener("channel-component", (uuid, byteArray) -> {
            ChatChannel channel = carbonChat.getChannelManager().getRegistry().get(byteArray.readUTF());
            ChatUser user = carbonChat.getUserService().wrap(uuid);

            if (channel != null) {
                channel.sendComponent(user, gsonSerializer.deserialize(byteArray.readUTF()));
            }
        });

        getMessageService().registerUUIDMessageListener("whisper-component", (uuid, byteArray) -> {
            UUID recipient = new UUID(byteArray.readLong(), byteArray.readLong());

            ChatUser target = carbonChat.getUserService().wrap(recipient);
            String message = byteArray.readUTF();

            if (!target.isIgnoringUser(uuid)) {
                target.setReplyTarget(uuid);
                target.sendMessage(gsonSerializer.deserialize(message));
            }
        });
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public void sendMessage(String key, UUID uuid, Consumer<ByteArrayDataOutput> consumer) {
        getMessageService().sendMessage(key, uuid, consumer);
    }

}

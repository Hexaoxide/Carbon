package net.draycia.simplechat.managers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.leonardosnt.bungeechannelapi.BungeeChannelApi;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PluginMessageManager implements PluginMessageListener {

    private final SimpleChat simpleChat;
    private final BungeeChannelApi api;

    public PluginMessageManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        simpleChat.getServer().getMessenger().registerOutgoingPluginChannel(simpleChat, "BungeeCord");
        simpleChat.getServer().getMessenger().registerIncomingPluginChannel(simpleChat, "BungeeCord", this);

        api = BungeeChannelApi.of(simpleChat);
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput input = ByteStreams.newDataInput(message);

        String subchannel = input.readUTF();
        short length = input.readShort();
        byte[] msgbytes = new byte[length];
        input.readFully(msgbytes);

        ByteArrayDataInput msg = ByteStreams.newDataInput(msgbytes);

        switch (subchannel) {
            case "simplechat:message":
                onMessageReceived(msg);
                break;
            case "simplechat:component":
                onComponentReceived(msg);
                break;
            case "simplechat:whisper":
                onWhisperReceived(msg);
                break;
        }
    }

    private void onMessageReceived(ByteArrayDataInput in) {
        String chatChannelName = in.readUTF();

        ChatChannel chatChannel = simpleChat.getChannelManager().getRegistry().get(chatChannelName);

        if (chatChannel == null) {
            return;
        }

        UUID playerUUID;

        try {
            playerUUID = UUID.fromString(in.readUTF());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        ChatUser user = simpleChat.getUserService().wrap(playerUUID);

        String chatMessage = in.readUTF();

        Bukkit.getScheduler().runTaskAsynchronously(simpleChat, () -> {
            chatChannel.sendMessage(user, chatMessage, true);
        });
    }

    public void onComponentReceived(ByteArrayDataInput in) {
        String chatChannelName = in.readUTF();

        ChatChannel chatChannel = simpleChat.getChannelManager().getRegistry().get(chatChannelName);

        if (chatChannel == null) {
            return;
        }

        UUID playerUUID;

        try {
            playerUUID = UUID.fromString(in.readUTF());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        ChatUser user = simpleChat.getUserService().wrap(playerUUID);

        String chatMessage = in.readUTF();

        Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
            chatChannel.sendComponent(user, simpleChat.getAdventureManager().processMessage(chatMessage, "br", "\n"));
        });
    }

    private void onWhisperReceived(ByteArrayDataInput in) {
        UUID targetUUID;

        try {
            targetUUID = UUID.fromString(in.readUTF());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        String chatMessage = in.readUTF();

        ChatUser target = simpleChat.getUserService().wrap(targetUUID);
        target.sendMessage(simpleChat.getAdventureManager().processMessage(chatMessage, "br", "\n"));
    }

    public void sendMessage(ChatChannel chatChannel, Player player, String message) {
        ByteArrayDataOutput msg = ByteStreams.newDataOutput();

        msg.writeUTF(chatChannel.getName());
        msg.writeUTF(player.getUniqueId().toString());
        msg.writeUTF(message);

        api.forward("ALL", "simplechat:message", msg.toByteArray());
    }

    public void sendComponent(ChatChannel chatChannel, Player player, Component component) {
        ByteArrayDataOutput msg = ByteStreams.newDataOutput();

        msg.writeUTF(chatChannel.getName());
        msg.writeUTF(player.getUniqueId().toString());
        msg.writeUTF(MiniMessage.get().serialize(component));

        api.forward("ALL", "simplechat:component", msg.toByteArray());
    }

    public void sendComponentToPlayer(ChatUser sender, ChatUser target, Component toComponent, Component fromComponent) {
        api.getPlayerList("ALL").whenComplete((result, error) -> {
            if (!result.contains(target.asOfflinePlayer().getName())) {
                return;
            }

            sender.sendMessage(toComponent);

            if (sender.isShadowMuted()) {
                return;
            }

            ByteArrayDataOutput msg = ByteStreams.newDataOutput();

            msg.writeUTF(target.getUUID().toString());
            msg.writeUTF(MiniMessage.get().serialize(fromComponent));

            api.forward("ALL", "simplechat:whisper", msg.toByteArray());
        });
    }
}

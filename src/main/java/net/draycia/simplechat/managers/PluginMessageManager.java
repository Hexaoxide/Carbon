package net.draycia.simplechat.managers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.minidigger.minimessage.text.MiniMessageParser;
import me.minidigger.minimessage.text.MiniMessageSerializer;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class PluginMessageManager implements PluginMessageListener {

    private SimpleChat simpleChat;

    public PluginMessageManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        simpleChat.getServer().getMessenger().registerOutgoingPluginChannel(simpleChat, "simplechat:message");
        simpleChat.getServer().getMessenger().registerIncomingPluginChannel(simpleChat, "simplechat:message", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals("simplechat:message")) {
            onMessageReceived(channel, player, message);
        } else if (channel.equals("simplechat:component")) {
            onComponentReceived(channel, player, message);
        }
    }

    private void onMessageReceived(String channel, Player player, byte[] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        String chatChannelName = in.readUTF();

        ChatChannel chatChannel = simpleChat.getChannel(chatChannelName);

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

        OfflinePlayer messageAuthor = Bukkit.getOfflinePlayer(playerUUID);

        String chatMessage = in.readUTF();

        Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
            chatChannel.sendMessage(messageAuthor, chatMessage);
        });
    }

    public void onComponentReceived(String channel, Player player, byte[] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        String chatChannelName = in.readUTF();

        ChatChannel chatChannel = simpleChat.getChannel(chatChannelName);

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

        OfflinePlayer messageAuthor = Bukkit.getOfflinePlayer(playerUUID);

        String chatMessage = in.readUTF();

        Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
            chatChannel.sendComponent(messageAuthor, MiniMessageParser.parseFormat(chatMessage));
        });
    }

    public void sendMessage(ChatChannel chatChannel, Player player, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(chatChannel.getName());
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(message);

        player.sendPluginMessage(simpleChat, "simplechat:message", out.toByteArray());
    }

    public void sendComponent(ChatChannel chatChannel, Player player, Component component) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(chatChannel.getName());
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(MiniMessageSerializer.serialize(component));

        player.sendPluginMessage(simpleChat, "simplechat:component", out.toByteArray());
    }
}

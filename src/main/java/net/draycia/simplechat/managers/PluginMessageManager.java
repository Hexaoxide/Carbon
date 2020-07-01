package net.draycia.simplechat.managers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class PluginMessageManager implements PluginMessageListener {

    private SimpleChat simpleChat;

    public PluginMessageManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        simpleChat.getServer().getMessenger().registerOutgoingPluginChannel(simpleChat, "BungeeCord");
        simpleChat.getServer().getMessenger().registerIncomingPluginChannel(simpleChat, "BungeeCord", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput input = ByteStreams.newDataInput(message);

        String subchannel = input.readUTF();
        short length = input.readShort();
        byte[] msgbytes = new byte[length];
        input.readFully(msgbytes);

        ByteArrayDataInput msg = ByteStreams.newDataInput(msgbytes);

        if (subchannel.equals("simplechat:message")) {
            onMessageReceived(msg);
        } else if (subchannel.equals("simplechat:component")) {
            onComponentReceived(msg);
        }
    }

    private void onMessageReceived(ByteArrayDataInput in) {
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

    public void onComponentReceived(ByteArrayDataInput in) {
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
            chatChannel.sendComponent(messageAuthor, MiniMessage.instance().parse(chatMessage));
        });
    }

    public void sendMessage(ChatChannel chatChannel, Player player, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("simplechat:message");

        ByteArrayDataOutput msg = ByteStreams.newDataOutput();

        msg.writeUTF(chatChannel.getName());
        msg.writeUTF(player.getUniqueId().toString());
        msg.writeUTF(message);

        out.writeShort(msg.toByteArray().length);
        out.write(msg.toByteArray());

        player.sendPluginMessage(simpleChat, "BungeeCord", out.toByteArray());
    }

    public void sendComponent(ChatChannel chatChannel, Player player, Component component) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("simplechat:component");

        ByteArrayDataOutput msg = ByteStreams.newDataOutput();

        msg.writeUTF(chatChannel.getName());
        msg.writeUTF(player.getUniqueId().toString());
        msg.writeUTF(MiniMessage.instance().serialize(component));

        out.writeShort(msg.toByteArray().length);
        out.write(msg.toByteArray());

        player.sendPluginMessage(simpleChat, "BungeeCord", out.toByteArray());
    }
}

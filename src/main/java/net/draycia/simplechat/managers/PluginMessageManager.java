package net.draycia.simplechat.managers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class PluginMessageManager implements PluginMessageListener {

    private SimpleChat simpleChat;

    public PluginMessageManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        simpleChat.getServer().getMessenger().registerIncomingPluginChannel(simpleChat, "BungeeCord", this);
        simpleChat.getServer().getMessenger().registerOutgoingPluginChannel(simpleChat, "BungeeCord");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String messageSubChannel = in.readUTF();

        if (!messageSubChannel.equalsIgnoreCase("simplechat-message")) {
            return;
        }

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

        chatChannel.sendMessage(messageAuthor, chatMessage);
    }

    public void sendMessage(ChatChannel chatChannel, OfflinePlayer player, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("simplechat-message");
        out.writeUTF(chatChannel.getName());
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(message);
    }
}
